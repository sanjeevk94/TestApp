package com.scubearena.testapp;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.provider.ContactsContract;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import nl.dionsegijn.konfetti.KonfettiView;
import nl.dionsegijn.konfetti.models.Shape;
import nl.dionsegijn.konfetti.models.Size;

public class OfferViewHolder extends RecyclerView.ViewHolder{
    private TextView textViewView;
    private ImageView imageView;
    private DatabaseReference mUsersDatabaseRef;
    String recName;
    String recToken;
    String recUid;
    View mView;

    public OfferViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        textViewView =  itemView.findViewById(R.id.text);
        imageView =  itemView.findViewById(R.id.store_image);
        }

    public void bind(Offer myOffer){
        textViewView.setText(myOffer.getText());
        Picasso.get().load(myOffer.getImageUrl()).centerCrop().fit().into(imageView);
    }

    public void setName(String name){

        TextView userNameView =  mView.findViewById(R.id.text);
        userNameView.setText(name);

    }

    public void setImage(String thumb_image){

        ImageView storeImage = mView.findViewById(R.id.store_image);
        Picasso.get().load(thumb_image).placeholder(R.drawable.ic_launcher_background).into(storeImage);

    }

    public void setSend(String sendText, String sendTo, final String gift, final String giftImage)
    {
        Button sendTextView =  mView.findViewById(R.id.send_text);
        sendTextView.setText(sendText);
        if(sendText.equalsIgnoreCase("CLAIM"))
        {
            sendTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent giftsIntent = new Intent(mView.getContext(),GiftCodeActivity.class);
                    giftsIntent.putExtra("gift",gift);
                    giftsIntent.putExtra("giftimage",giftImage);
                    mView.getContext().startActivity(giftsIntent);
                }
            });

        }
        else if(sendText.equalsIgnoreCase("SEND"))
        {
            mUsersDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child(sendTo);
            mUsersDatabaseRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    recUid = dataSnapshot.getKey();
                    recName = dataSnapshot.child("name").getValue().toString();
                    recToken = dataSnapshot.child("device_token").getValue().toString();

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            sendTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    AlertDialog.Builder builderSingle = new AlertDialog.Builder(mView.getContext());
                    builderSingle.setIcon(R.drawable.default_contact);
                    builderSingle.setTitle("Do you want to send this gift to your buddy?");
                    builderSingle.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builderSingle.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            sendGift(recUid, recToken, gift, giftImage);
                            dialog.dismiss();
                            Intent startIntent = new Intent(mView.getContext(), Celebration.class);
                            mView.getContext().startActivity(startIntent);
                        }
                    });
                    builderSingle.show();
                }
            });
        }
    }

    public void setView(final String gift, final String giftImage)
    {
        final Button viewTextView =  mView.findViewById(R.id.view_det);


        final Dialog myDialog  = new Dialog(mView.getContext());
        //final Button btnFollow;

        viewTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    myDialog.setContentView(R.layout.custom_popup);
                    TextView txtclose = myDialog.findViewById(R.id.txtclose);
                    ImageView image = myDialog.findViewById(R.id.offerImage);
                    TextView offer = myDialog.findViewById(R.id.offer);
                    TextView offerDetail = myDialog.findViewById(R.id.offerDetail);

                    offer.setText(gift);
                    offerDetail.setText("This offer is applicable only for new users. Offer gets expired on 20th Feb 2019. Valid one time per user.");
                    Picasso.get().load(giftImage).placeholder(R.drawable.ic_launcher_background).into(image);
                    //Button btnFollow = myDialog.findViewById(R.id.btnfollow);
                    txtclose.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            myDialog.dismiss();
                        }
                    });
                    myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    myDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation_2;
                    myDialog.show();
                }
            });
        }


    public void sendGift(final String recUid, String recToken, String gift, String giftImage)
    {
        DatabaseReference mRootRef;
        FirebaseUser mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mRootRef  = FirebaseDatabase.getInstance().getReference();

        DatabaseReference mGiftsNotificationRef = mRootRef.child("Gifts_Notifications").child(recUid).push();
        String newGiftNotifId = mGiftsNotificationRef.getKey();


        HashMap<String,String> giftData = new HashMap<>();
        giftData.put("from",mCurrentUser.getUid());
        giftData.put("gift",gift);
        giftData.put("giftImage",giftImage);
        giftData.put("giftLocation","");

        HashMap<String,String> giftNotificationData = new HashMap<>();
        giftNotificationData.put("from",mCurrentUser.getUid());
        giftNotificationData.put("to",recUid);
        giftNotificationData.put("to_token",recToken);


        Map requestMap = new HashMap();
        requestMap.put("Gifts/"+recUid+"/"+newGiftNotifId,giftData);
        requestMap.put("Gifts_Notifications/"+newGiftNotifId,giftNotificationData);

        mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if(databaseError != null)
                {
                    Toasty.error(mView.getContext(),"There was an error in sending the request",Toast.LENGTH_LONG).show();
                }
                else {

                    Toasty.success(mView.getContext(),"Your Gift has been sent",Toast.LENGTH_LONG).show();
                    removeOffers(recUid);

                }
            }
        });


    }

    public void removeOffers(String recUid)
    {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String currentUser = mAuth.getCurrentUser().getUid();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        Date date = new Date();
        String currentDate = formatter.format(date);
        DatabaseReference mEventInfoDbRef = FirebaseDatabase.getInstance().getReference().child("EventInfo").child(currentDate).child(currentUser).child(recUid);
       final DatabaseReference mUserEventInfoDbRef = FirebaseDatabase.getInstance().getReference().child("EventUserInfo").child(currentDate).child(currentUser).child(recUid);

        mEventInfoDbRef.removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                mUserEventInfoDbRef.removeValue(new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                    }
                });
            }
        });

    }
}

