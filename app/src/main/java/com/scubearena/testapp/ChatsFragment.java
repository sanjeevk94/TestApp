package com.scubearena.testapp;


import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Iterator;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private RecyclerView mConvList;

    private DatabaseReference mConvDatabase;
    private DatabaseReference mMessageDatabase;
    private DatabaseReference mUsersDatabase;

    private FirebaseAuth mAuth;

    private String mCurrent_user_id;

    private View mMainView;

    private TextView noChats;
    FirebaseRecyclerAdapter<Conv, ConvViewHolder> firebaseConvAdapter;

    String searchText;

    private EditText search;

    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //searchText = getArguments().getString("searchText");
        mMainView = inflater.inflate(R.layout.fragment_chats, container, false);

        mConvList =  mMainView.findViewById(R.id.conv_list);

        noChats = mMainView.findViewById(R.id.no_chats);

        search = mMainView.findViewById(R.id.editText);


        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser() != null) {
            mCurrent_user_id = mAuth.getCurrentUser().getUid();

        mConvDatabase = FirebaseDatabase.getInstance().getReference().child("Chat").child(mCurrent_user_id);

        mConvDatabase.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mMessageDatabase = FirebaseDatabase.getInstance().getReference().child("messages").child(mCurrent_user_id);
        mUsersDatabase.keepSynced(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        mConvList.setHasFixedSize(true);
        mConvList.setLayoutManager(linearLayoutManager);

        }
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                firebaseContactSerach(s.toString());

            }
        });
        // Inflate the layout for this fragment
        return mMainView;
    }

    public void firebaseContactSerach(final String searchText)
    {
        if(!TextUtils.isEmpty(searchText)) {
            final Query firebaseSearchQuery = mUsersDatabase.orderByChild("name").startAt(searchText).endAt(searchText+ "\uf8ff");
            FirebaseRecyclerAdapter<Users, ConvViewHolder> firebaseAdapter;
            firebaseAdapter = new FirebaseRecyclerAdapter<Users, ConvViewHolder>(
                    Users.class,
                    R.layout.users_single_layout,
                    ConvViewHolder.class,
                    firebaseSearchQuery
            ) {
                @Override
                protected void populateViewHolder(final ConvViewHolder viewHolder, final Users model, int position) {

                    final String userId = getRef(position).getKey();

                    mConvDatabase.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                if (userId.equals(ds.getKey())) {
                                    viewHolder.setName(model.getName());
                                    viewHolder.setUserImage(model.getThumb_image());
                                    Query lastMessageQuery = mMessageDatabase.child(userId).limitToLast(1);
                                    lastMessageQuery.addChildEventListener(new ChildEventListener() {
                                        @Override
                                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                                            noChats.setVisibility(View.INVISIBLE);
                                            if(dataSnapshot.hasChild("messages")) {
                                                String data = dataSnapshot.child("messages").getValue().toString();
                                                if (data.contains("https://firebasestorage.googleapis.com/v0/b/testapp-99001.appspot.com/o/message_images")) {
                                                    viewHolder.setMessage("Image", true);
                                                } else {
                                                    viewHolder.setMessage(data, true);
                                                }
                                            }

                                        }

                                        @Override
                                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                                        }

                                        @Override
                                        public void onChildRemoved(DataSnapshot dataSnapshot) {

                                        }

                                        @Override
                                        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                            chatIntent.putExtra("user_id", userId);
                            chatIntent.putExtra("user_name", model.getName());
                            startActivity(chatIntent);
                        }
                    });

                }
            };

            mConvList.setAdapter(firebaseAdapter);
        }
        else
        {
            mConvList.setAdapter(firebaseConvAdapter);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        mConvList =  mMainView.findViewById(R.id.conv_list);

        noChats = mMainView.findViewById(R.id.no_chats);

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();

        Query conversationQuery = mConvDatabase.orderByChild("timestamp");
        firebaseConvAdapter = new FirebaseRecyclerAdapter<Conv, ConvViewHolder>(
                Conv.class,
                R.layout.users_single_layout,
                ConvViewHolder.class,
                conversationQuery
        ) {
            @Override
            protected void populateViewHolder(final ConvViewHolder convViewHolder, final Conv conv, int i) {



                final String list_user_id = getRef(i).getKey();
                Query lastMessageQuery = mMessageDatabase.child(list_user_id).limitToLast(1);
                lastMessageQuery.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                        noChats.setVisibility(View.INVISIBLE);
                        if(dataSnapshot.hasChild("messages")) {
                            String data = dataSnapshot.child("messages").getValue().toString();
                            if (data.contains("https://firebasestorage.googleapis.com/v0/b/testapp-99001.appspot.com/o/message_images")) {
                                convViewHolder.setMessage("Image", conv.isSeen());
                            } else {
                                convViewHolder.setMessage(data, conv.isSeen());
                            }
                        }

                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if (dataSnapshot.hasChild("name")) {
                                noChats.setVisibility(View.INVISIBLE);
                                final String userName = dataSnapshot.child("name").getValue().toString();
                                String userThumb = dataSnapshot.child("thumb_image").getValue().toString();

                                if (dataSnapshot.hasChild("online")) {

                                    String userOnline = dataSnapshot.child("online").getValue().toString();
                                    convViewHolder.setUserOnline(userOnline);

                                }

                                convViewHolder.setName(userName);
                                convViewHolder.setUserImage(userThumb);

                                convViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                        chatIntent.putExtra("user_id", list_user_id);
                                        chatIntent.putExtra("user_name", userName);
                                        startActivity(chatIntent);

                                    }
                                });
                                convViewHolder.mView.setOnLongClickListener(new View.OnLongClickListener() {
                                    @Override
                                    public boolean onLongClick(View v) {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                        builder.setMessage("Do you want to delete chat with " + userName)
                                                .setCancelable(false)
                                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference().child("Chat").child(mCurrent_user_id);
                                                        Query deleteChatRef = chatRef.child(list_user_id);
                                                        deleteChatRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                for (DataSnapshot deleteSnapshot : dataSnapshot.getChildren()) {
                                                                    deleteSnapshot.getRef().removeValue();
                                                                }
                                                                DatabaseReference msgRef = FirebaseDatabase.getInstance().getReference().child("messages").child(mCurrent_user_id);
                                                                Query deleteMsgRef = msgRef.child(list_user_id);
                                                                deleteMsgRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                                                        for (DataSnapshot deleteMsgSnapshot : dataSnapshot.getChildren()) {
                                                                            deleteMsgSnapshot.getRef().removeValue();
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


                                                    }
                                                })
                                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {

                                                        dialog.cancel();

                                                    }
                                                });
                                        AlertDialog alert = builder.create();
                                        alert.setTitle("Delete Chat");
                                        alert.show();

                                        return true;
                                    }
                                });

                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
            }
        };

        mConvList.setAdapter(firebaseConvAdapter);

    }

    public static class ConvViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public ConvViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

        }

        public void setMessage(String message, boolean isSeen){

            TextView userStatusView =  mView.findViewById(R.id.prof_status);
            userStatusView.setText(message);

            if(!isSeen){
                userStatusView.setTypeface(userStatusView.getTypeface(), Typeface.BOLD);
            } else {
                userStatusView.setTypeface(userStatusView.getTypeface(), Typeface.NORMAL);
            }

        }

        public void setName(String name){

            TextView userNameView =  mView.findViewById(R.id.prof_name);
            userNameView.setText(name);

        }

        public void setUserImage(String thumb_image){

            CircleImageView userImageView =  mView.findViewById(R.id.prof_img);
            Picasso.get().load(thumb_image).placeholder(R.drawable.default_contact).into(userImageView);

        }

        public void setUserOnline(String online_status) {

            ImageView userOnlineView =  mView.findViewById(R.id.prof_online_icon);

            if(online_status.equals("true")){

                userOnlineView.setVisibility(View.VISIBLE);
            }
            else {
                userOnlineView.setVisibility(View.INVISIBLE);

            }

        }


    }




}