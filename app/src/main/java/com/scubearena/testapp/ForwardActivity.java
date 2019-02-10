package com.scubearena.testapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.dmoral.toasty.Toasty;


public class ForwardActivity extends AppCompatActivity implements ActionMode.Callback{

    private RecyclerView mConvList;
    private String forwardedText;

    private Toolbar mToolbar;


    private FirebaseAuth mAuth;
    private String mCurrent_user_id;

    private ActionMode actionMode;
    private boolean isMultiSelect = false;
    private List<String> selectedIds = new ArrayList<>();
    private List<Friends> FriendsList = new ArrayList<>();
    private FriendsAdapter mAdapter;

    private DatabaseReference mRootRef;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forward);

        mConvList = findViewById(R.id.fowardList);
        mToolbar = findViewById(R.id.fwd_appBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Forward to....");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mRootRef = FirebaseDatabase.getInstance().getReference();

        forwardedText = getIntent().getStringExtra("textToBeForwarded");
        System.out.println("********** Text to be forwarded : "+forwardedText);



        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {

            mCurrent_user_id = mAuth.getCurrentUser().getUid();
            prepareFriendsData();
            mAdapter = new FriendsAdapter(FriendsList);

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            linearLayoutManager.setReverseLayout(true);
            linearLayoutManager.setStackFromEnd(true);
            mConvList.setHasFixedSize(true);
            mConvList.setLayoutManager(linearLayoutManager);
            mConvList.setAdapter(mAdapter);
            mConvList.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), mConvList, new RecyclerTouchListener.ClickListener() {
                @Override
                public void onClick(View view, int position) {
                    if(isMultiSelect)
                    {
                        multiSelect(position);
                    }
                }

                @Override
                public void onLongClick(View view, int position) {

                    if(!isMultiSelect)
                    {
                        selectedIds = new ArrayList<>();
                        isMultiSelect = true;

                        if(actionMode == null)
                        {
                            actionMode = startActionMode(ForwardActivity.this);
                        }
                    }
                    multiSelect(position);

                }
            }));

        }
    }

    private void handleSendImage(Intent intent,final String mChatUser) {

        mProgressDialog = new ProgressDialog(ForwardActivity.this,R.style.MyAlertDialogStyle);
        mProgressDialog.setMessage("Please wait while we forward..");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        Uri imageUri =  intent.getParcelableExtra(Intent.EXTRA_STREAM);
        StorageReference mImageStorage = FirebaseStorage.getInstance().getReference();

        final String mCurrentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (imageUri != null)
        {

            final String current_user_ref = "messages/" + mCurrentUserId + "/" + mChatUser;
            final String chat_user_ref = "messages/" + mChatUser + "/" + mCurrentUserId;

            DatabaseReference user_message_push = mRootRef.child("messages")
                    .child(mCurrentUserId).child(mChatUser).push();

            final String push_id = user_message_push.getKey();


            StorageReference filepath = mImageStorage.child("message_images").child(push_id + ".jpg");

            filepath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                    if (task.isSuccessful()) {

                        String download_url = task.getResult().getDownloadUrl().toString();


                        Map messageMap = new HashMap();
                        messageMap.put("messages", download_url);
                        messageMap.put("seen", false);
                        messageMap.put("type", "image");
                        messageMap.put("time", ServerValue.TIMESTAMP);
                        messageMap.put("from", mCurrentUserId);
                        messageMap.put("to", mChatUser);


                        HashMap<String, String> notificationData = new HashMap<>();
                        notificationData.put("from", mCurrentUserId);
                        notificationData.put("type", "request");

                        Map messageUserMap = new HashMap();
                        messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
                        messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);
                        messageUserMap.put("message_notifications/" + mChatUser + "/" + push_id, notificationData);

                        mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if (databaseError != null) {

                                    Log.d("CHAT_LOG", databaseError.getMessage());

                                }

                            }

                        });
                        mProgressDialog.hide();
                    }

                }
            });
        }
        else
            {
            Toast.makeText(this, "Error occured, URI is invalid", Toast.LENGTH_LONG).show();
            }
    }


    private void multiSelect(int position)
    {
        Friends friend = mAdapter.getItem(position);
        if(friend != null)
        {

            if(actionMode != null)
            {

                if(selectedIds.contains(friend.getId()))
                {
                    selectedIds.remove(friend.getId());

                }
                else
                {
                    selectedIds.add(friend.getId());
                }
                if(selectedIds.size() > 0)
                {
                    actionMode.setTitle(String.valueOf(selectedIds.size()));
                }
                else
                {
                    actionMode.setTitle("");
                    actionMode.finish();
                }
                mAdapter.setSelectedIds(selectedIds);
                mAdapter.notifyDataSetChanged();
             }
        }
    }

    public void prepareFriendsData()
    {

        Query friends = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrent_user_id).orderByChild("date");
        friends.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot friendsData : dataSnapshot.getChildren())
                {
                    String id = friendsData.getKey();
                    DatabaseReference userData = FirebaseDatabase.getInstance().getReference().child("Users").child(id);
                    userData.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String name = dataSnapshot.child("name").getValue().toString();
                            String image = dataSnapshot.child("image").getValue().toString();
                            String status = dataSnapshot.child("status").getValue().toString();
                            Friends friend = new Friends(dataSnapshot.getKey(),name,image,status);
                            FriendsList.add(friend);
                            mAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

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
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.forward_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.frwd:
                StringBuilder stringBuilder = new StringBuilder();
                String Id="";
                for (Friends data : FriendsList)
                {
                    Id = data.getId();
                    if (selectedIds.contains(data.getId()))
                    {
                        stringBuilder.append("\n").append(data.getName());
                        if(forwardedText == null)
                        {
                            Intent intent = getIntent();

                            String action = intent.getAction();
                            String type = intent.getType();
                            if (Intent.ACTION_SEND.equals(action) && type != null) {

                                if ("text/plain".equals(type)) {

                                } else if (type.startsWith("image/")) {
                                    handleSendImage(intent,Id);
                                }
                            }
                        }
                        else {
                            sendMessage(forwardedText, Id);
                        }
                    }
                }
                Intent chatIntent = new Intent(ForwardActivity.this,MainActivity.class);
                startActivity(chatIntent);
                finish();
                return true;
        }
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        actionMode = null;
        isMultiSelect = false;
        selectedIds = new ArrayList<>();
        mAdapter.setSelectedIds(new ArrayList<String>());
    }

    private void sendMessage(String message,String mChatUser)
    {

        DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();

        String mCurrentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if(!TextUtils.isEmpty(message))
        {
            String currentUserRef = "messages/"+mCurrentUserId+"/"+mChatUser;
            String chatUserRef = "messages/"+mChatUser+"/"+mCurrentUserId;

            DatabaseReference userMessagePush = mRootRef.child("messages").child(mCurrentUserId).child(mChatUser).push();

            String pushId = userMessagePush.getKey();

            Map messageMap = new HashMap();
            messageMap.put("messages",message);
            messageMap.put("seen",false);
            if(message.contains("https://firebasestorage.googleapis.com/v0/b/testapp-99001.appspot.com/o/message_images"))
            {
                messageMap.put("type","image");

            }
            else
            {
                messageMap.put("type","text");
            }
            messageMap.put("time",ServerValue.TIMESTAMP);
            messageMap.put("from",mCurrentUserId);

        /*DatabaseReference newNotificationRef = mRootRef.child("notifications").child(mChatUser).push();
        //String newNotificationId = newNotificationRef.getKey();*/

            HashMap<String,String> notificationData = new HashMap<>();
            notificationData.put("from",mCurrentUserId);
            notificationData.put("type","request");
            
            Map messageUserMap = new HashMap();
            messageUserMap.put(currentUserRef+"/"+pushId,messageMap);
            messageUserMap.put(chatUserRef+"/"+pushId,messageMap);
            messageUserMap.put("message_notifications/"+mChatUser+"/"+pushId,notificationData);

            mRootRef.child("Chat").child(mCurrentUserId).child(mChatUser).child("seen").setValue(true);
            mRootRef.child("Chat").child(mCurrentUserId).child(mChatUser).child("timestamp").setValue(ServerValue.TIMESTAMP);

            mRootRef.child("Chat").child(mChatUser).child(mCurrentUserId).child("seen").setValue(false);
            mRootRef.child("Chat").child(mChatUser).child(mCurrentUserId).child("timestamp").setValue(ServerValue.TIMESTAMP);

            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                    if(databaseError != null)
                    {
                        Log.d("CHAT_LOG",databaseError.getMessage());
                    }
                }
            });

        }
    }

    @Override
    public void onDestroy() {
        if(mProgressDialog != null)
        {
            mProgressDialog.dismiss();
        }
        super.onDestroy();

    }


}
