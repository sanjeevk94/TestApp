package com.scubearena.testapp;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;

public class ProfileActivity extends AppCompatActivity {

    private ImageView profileImage;
    private TextView profileName,profileStatus;
    private Button sendFriendRequest,declineFriendRequest;
    private DatabaseReference mUserDatabase;
    private ProgressDialog mProgressDialog;

    private DatabaseReference mFriendReqDatabase;
    private DatabaseReference friendsDatabase;
    private DatabaseReference notificationDatabase;
    private DatabaseReference mRootRef;
    private FirebaseUser mCurrentUser;
    private String mCurrentStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String uId = getIntent().getStringExtra("user_id");

        mRootRef = FirebaseDatabase.getInstance().getReference();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uId);
        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("FriendRequests");
        friendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        notificationDatabase = FirebaseDatabase.getInstance().getReference().child("notifications");
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        profileImage = findViewById(R.id.user_profile_img);
        profileName = findViewById(R.id.user_profile_name);
        profileStatus = findViewById(R.id.user_profile_status);
        sendFriendRequest = findViewById(R.id.user_send_req_btn);
        declineFriendRequest = findViewById(R.id.user_dec_req_btn);


        mCurrentStatus ="not_friends";
        declineFriendRequest.setVisibility(View.INVISIBLE);
        declineFriendRequest.setEnabled(false);


        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Please wait while loading user profile...");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();


        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                String email = dataSnapshot.child("email").getValue().toString();
                String prfPicStatus = dataSnapshot.child("profile_pic_hide").getValue().toString();
                String prfStatus = dataSnapshot.child("profile_status_hide").getValue().toString();
                String contactStatus = dataSnapshot.child("profile_contact_details").getValue().toString();

                if("Y".equalsIgnoreCase(prfPicStatus))
                {
                    profileImage.setImageResource(R.drawable.default_contact);
                }
                else
                {
                    Picasso.get().load(image).placeholder(R.drawable.default_contact).into(profileImage);

                }

                if("Y".equalsIgnoreCase(prfStatus))
                {
                    profileStatus.setText("Hi there, I am using Gift app");

                }
                else
                {
                    profileStatus.setText(status);

                }
                profileName.setText(name);

                if(mCurrentUser.getUid().equals(uId)){

                    declineFriendRequest.setEnabled(false);
                    declineFriendRequest.setVisibility(View.INVISIBLE);

                    sendFriendRequest.setEnabled(false);
                    sendFriendRequest.setVisibility(View.INVISIBLE);

                }

                //----------------- FRIENDS LIST / REQUEST FEATURE ---------------------
                mFriendReqDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(uId))
                        {
                            String req_type = dataSnapshot.child(uId).child("request_type").getValue().toString();
                               if(req_type.equals("received"))
                            {
                                mCurrentStatus = "req_received";
                               // sendFriendRequest.setText("ACCEPT FRIEND REQUEST");
                                sendFriendRequest.setText("FOLLOW");
                                declineFriendRequest.setVisibility(View.VISIBLE);
                                declineFriendRequest.setEnabled(true);
                            }
                            else if(req_type.equals("sent"))
                            {
                                mCurrentStatus = "req_sent";
                                //sendFriendRequest.setText("CANCEL FRIEND REQUEST");
                                sendFriendRequest.setText("CANCEL");
                                declineFriendRequest.setVisibility(View.INVISIBLE);
                                declineFriendRequest.setEnabled(false);
                            }
                            mProgressDialog.dismiss();
                        }
                        else {
                            friendsDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(uId))
                                    {
                                        mCurrentStatus="friends";
                                       // sendFriendRequest.setText("UNFRIEND THIS PERSON");
                                        sendFriendRequest.setText("UNFOLLOW");
                                        declineFriendRequest.setVisibility(View.INVISIBLE);
                                        declineFriendRequest.setEnabled(false);
                                    }
                                    mProgressDialog.dismiss();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    mProgressDialog.dismiss();
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        sendFriendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendFriendRequest.setEnabled(false);

                //-------------  NOT FRIENDS STATE ----------------------

                if(mCurrentStatus.equals("not_friends"))
                {

                    DatabaseReference newNotificationRef = mRootRef.child("notifications").child(uId).push();
                    String newNotificationId = newNotificationRef.getKey();

                    HashMap<String,String> notificationData = new HashMap<>();
                    notificationData.put("from",mCurrentUser.getUid());
                    notificationData.put("type","request");

                    Map requestMap = new HashMap();
                    requestMap.put("FriendRequests/"+mCurrentUser.getUid()+"/"+uId+ "/request_type","sent");
                    requestMap.put("FriendRequests/"+mCurrentUser.getUid()+"/"+uId+ "/status","sent");
                    requestMap.put("FriendRequests/"+uId+"/"+mCurrentUser.getUid()+ "/request_type","received");
                    requestMap.put("FriendRequests/"+uId+"/"+mCurrentUser.getUid()+ "/status","received");
                    requestMap.put("notifications/"+uId+"/"+newNotificationId,notificationData);

                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError != null)
                            {
                                Toasty.success(ProfileActivity.this,"There was an error in sending the request",Toast.LENGTH_LONG).show();
                            }
                            else {
                                mCurrentStatus = "req_sent";
                                //sendFriendRequest.setText("CANCEL FRIEND REQUEST");
                                sendFriendRequest.setText("CANCEL");
                            }
                            sendFriendRequest.setEnabled(true);
                        }
                    });

                }

                //----------------- CANCEL REQUEST STATE ---------------------
                if(mCurrentStatus.equals("req_sent"))
                {
                    mFriendReqDatabase.child(mCurrentUser.getUid()).child(uId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendReqDatabase.child(uId).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    sendFriendRequest.setEnabled(true);
                                    mCurrentStatus="not_friends";
                                    //sendFriendRequest.setText("SEND FRIEND REQUEST");
                                    sendFriendRequest.setText("FOLLOW ME");
                                    declineFriendRequest.setVisibility(View.INVISIBLE);
                                    declineFriendRequest.setEnabled(false);
                                }
                            });
                        }
                    });
                }

                //------------------ REQUEST RECEIVED STATE ---------------------

                if(mCurrentStatus.equals("req_received"))
                {
                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());


                    Map friendsMap = new HashMap();
                    friendsMap.put("Friends/"+mCurrentUser.getUid()+"/"+uId+"/date",currentDate);
                    friendsMap.put("Friends/"+uId+"/"+mCurrentUser.getUid()+"/date",currentDate);

                    friendsMap.put("FriendRequests/"+mCurrentUser.getUid()+"/"+uId,null);
                    friendsMap.put("FriendRequests/"+uId+"/"+mCurrentUser.getUid(),null);

                    mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError == null)
                            {
                                sendFriendRequest.setEnabled(true);
                                mCurrentStatus="friends";
                                //sendFriendRequest.setText("UNFRIEND THIS PERSON");
                                sendFriendRequest.setText("UNFOLLOW");
                                declineFriendRequest.setVisibility(View.INVISIBLE);
                                declineFriendRequest.setEnabled(false);

                            }
                            else
                            {
                                String error = databaseError.getMessage();
                                Toasty.error(ProfileActivity.this,error,Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }

                //--------------------- UNFRIENDS ----------------------

                if(mCurrentStatus.equals("friends"))
                {

                    Map unFriendMap = new HashMap();
                    unFriendMap.put("Friends/"+mCurrentUser.getUid()+"/"+uId,null);
                    unFriendMap.put("Friends/"+uId+"/"+mCurrentUser.getUid(),null);
                    mRootRef.updateChildren(unFriendMap, new DatabaseReference.CompletionListener()
                    {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError == null)
                            {
                                mCurrentStatus = "not_friends";
                                //sendFriendRequest.setText("SEND FRIEND REQUEST");
                                sendFriendRequest.setText("FOLLOW ME");
                                declineFriendRequest.setVisibility(View.INVISIBLE);
                                declineFriendRequest.setEnabled(false);
                            }
                            else
                            {
                                String error = databaseError.getMessage();
                                Toasty.error(ProfileActivity.this,error,Toast.LENGTH_LONG).show();
                            }
                            sendFriendRequest.setEnabled(true);
                        }
                    });
                    /*Map delMsgMap = new HashMap();
                    delMsgMap.put("messages/"+mCurrentUser.getUid()+"/"+uId,null);
                    delMsgMap.put("messages/"+uId+"/"+mCurrentUser.getUid(),null);
                    mRootRef.updateChildren(delMsgMap,new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError == null)
                            {
                                Map deleteChatMap = new HashMap();
                                deleteChatMap.put("Chat/"+mCurrentUser.getUid()+"/"+uId,null);
                                deleteChatMap.put("Chat/"+uId+"/"+mCurrentUser.getUid(),null);
                                mRootRef.updateChildren(deleteChatMap, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                        if(databaseError == null)
                                        {

                                      }
                                    }
                                });
                            }
                        }
                    });*/
                }
            }
        });


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
