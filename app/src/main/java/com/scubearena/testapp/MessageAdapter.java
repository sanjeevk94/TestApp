package com.scubearena.testapp;


import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import hani.momanii.supernova_emoji_library.Helper.EmojiconTextView;

public class MessageAdapter extends RecyclerView.Adapter {


    private List<Messages> mMessageList;
    private DatabaseReference mUserDatabase;
    private FirebaseAuth mAuth;
    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;



    public MessageAdapter(List<Messages> mMessageList) {

        this.mMessageList = mMessageList;

    }

    @Override
    public int getItemViewType(int position) {
        Messages message = mMessageList.get(position);
        mAuth = FirebaseAuth.getInstance();

        if(message != null && message.getFrom()!=null) {
            if (message.getFrom().equals(mAuth.getCurrentUser().getUid())) {
                // If the current user is the sender of the message
                return VIEW_TYPE_MESSAGE_SENT;
            } else {
                // If some other user sent the message
                return VIEW_TYPE_MESSAGE_RECEIVED;
            }
        }
        return 0;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageHolder(view);
        } else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageHolder(view);
        }

        return null;
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Messages message = mMessageList.get(position);

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MESSAGE_SENT:
                ((SentMessageHolder) holder).bind(message);
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED:
                ((ReceivedMessageHolder) holder).bind(message);
        }
    }

    //For Sender Part

    private class SentMessageHolder extends RecyclerView.ViewHolder {
        private Context context;
        TextView  timeText;
        EmojiconTextView messageText;
        ImageView messageImage;
        public int position=0;

        SentMessageHolder(View itemView) {
            super(itemView);

            context = itemView.getContext();
            messageText =  itemView.findViewById(R.id.text_message_body);
            timeText = itemView.findViewById(R.id.text_message_time);
            messageImage = itemView.findViewById(R.id.sen_message_image_layout);



        }

        void bind(final Messages message) {

            //messageText.setText(message.getMessages());
            String message_type = message.getType();
            if(message.isSeen())
            {
                messageText.setBackgroundResource(R.drawable.background_for_sender_msg);

            }
            else
            {
                messageText.setBackgroundResource(R.drawable.background_for_sender_msg_temp);
            }
            if(message_type.equals("text"))
            {

                messageText.setText(message.getMessages());
                messageImage.setVisibility(View.INVISIBLE);
            }
            else {

                messageText.setVisibility(View.INVISIBLE);
                Picasso.get().load(message.getMessages()).resize(500,500).transform(new PicassoCircleTransformation(5,25))
                        .placeholder(R.drawable.progress_animation).into(messageImage);

            }
            messageImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /*Intent fullScreenIntent = new Intent(context,FullScreenImageActivity.class);
                    fullScreenIntent.putExtra("imageUrl",message.getMessages());
                    context.startActivity(fullScreenIntent);*/
                }
            });

            // Format the stored timestamp into a readable String using method.
            final long time = message.getTime();
            final SimpleDateFormat sfd = new SimpleDateFormat("dd-MM HH:mm");
            timeText.setText(sfd.format(new Date(time)));
        }

    }


    //For Receiver part.

    private class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        private Context context;

        TextView  timeText, nameText;
        EmojiconTextView messageText;
        ImageView profileImage,messageImage;

        ReceivedMessageHolder(View itemView) {
            super(itemView);
            context = itemView.getContext();
            messageText = itemView.findViewById(R.id.text_message_body);
            timeText = itemView.findViewById(R.id.text_message_time);
            nameText = itemView.findViewById(R.id.text_message_name);
            profileImage = itemView.findViewById(R.id.image_message_profile);
            messageImage = itemView.findViewById(R.id.rec_message_image_layout);

        }

        void bind(final Messages message) {

            String message_type = message.getType();
            if(message_type.equals("text"))
            {
                messageText.setText(message.getMessages());
                messageImage.setVisibility(View.INVISIBLE);


            }
            else {

                messageText.setVisibility(View.INVISIBLE);
                Picasso.get().load(message.getMessages()).resize(500,500).transform(new PicassoCircleTransformation(5,25))
                        .placeholder(R.drawable.progress_animation).into(messageImage);

            }

            messageText.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Toast.makeText(context, message.getMessages(), Toast.LENGTH_LONG).show();
                    return true;
                }
            });
            messageImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent fullScreenIntent = new Intent(context,FullScreenImageActivity.class);
                    fullScreenIntent.putExtra("imageUrl",message.getMessages());
                    context.startActivity(fullScreenIntent);
                }
            });



            // Format the stored timestamp into a readable String using method.
            final long time = message.getTime();
            final SimpleDateFormat sfd = new SimpleDateFormat("dd-MM HH:mm");
            timeText.setText(sfd.format(new Date(time)));

            mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(message.getFrom());
            mUserDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                   String userName = dataSnapshot.child("name").getValue().toString();
                   String image= dataSnapshot.child("image").getValue().toString();
                   String profImgStatus = dataSnapshot.child("profile_pic_hide").getValue().toString();
                    nameText.setText(userName);
                    if("Y".equalsIgnoreCase(profImgStatus))
                    {
                        profileImage.setImageResource(R.drawable.default_contact);
                    }
                    else {
                        Picasso.get().load(image).placeholder(R.drawable.default_contact).into(profileImage);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

}
