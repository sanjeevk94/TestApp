package com.scubearena.testapp;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {

    private RecyclerView mFriendsList;

    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mUsersDatabase;

    private FirebaseAuth mAuth;

    private String mCurrent_user_id;

    private View mMainView;

    private TextView noFriends;

    private EditText search;

    private FirebaseRecyclerAdapter<Friends, FriendsViewHolder> friendsRecyclerViewAdapter;


    public FriendsFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mMainView = inflater.inflate(R.layout.fragment_friends, container, false);

        mFriendsList =  mMainView.findViewById(R.id.friends_list);
        noFriends = mMainView.findViewById(R.id.no_friends);
        search = mMainView.findViewById(R.id.search);

        mAuth = FirebaseAuth.getInstance();

        mCurrent_user_id = mAuth.getCurrentUser().getUid();

        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrent_user_id);
        mFriendsDatabase.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);


        mFriendsList.setHasFixedSize(true);
        mFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));

        FloatingActionButton fab1 = mMainView.findViewById(R.id.add_friends);
        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startIntent = new Intent(getActivity(),UsersActivity.class);
                startActivity(startIntent);

            }
        });

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
            final Query firebaseSearchQuery = mUsersDatabase.orderByChild("name").startAt(searchText).endAt(searchText + "\uf8ff");
            FirebaseRecyclerAdapter<Friends, FriendsViewHolder> firebaseAdapter;
            firebaseAdapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(
                    Friends.class,
                    R.layout.users_single_layout,
                    FriendsViewHolder.class,
                    firebaseSearchQuery
            ) {
                @Override
                protected void populateViewHolder(final FriendsViewHolder friendsViewHolder, final Friends model, int position) {

                    final String userId = getRef(position).getKey();

                    mFriendsDatabase.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            for (DataSnapshot ds : dataSnapshot.getChildren())
                            {
                                if (userId.equals(ds.getKey()))
                                {
                                    friendsViewHolder.setDate(ds.child("date").getValue().toString());
                                    mUsersDatabase.child(userId).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                            noFriends.setVisibility(View.INVISIBLE);

                                            if(dataSnapshot.hasChild("name")) {

                                                final String userName = dataSnapshot.child("name").getValue().toString();
                                                String userThumb = dataSnapshot.child("thumb_image").getValue().toString();

                                                if (dataSnapshot.hasChild("online")) {

                                                    String userOnline = dataSnapshot.child("online").getValue().toString();
                                                    friendsViewHolder.setUserOnline(userOnline);

                                                }
                                                friendsViewHolder.setName(userName);
                                                friendsViewHolder.setUserImage(userThumb);

                                                friendsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {

                                                        CharSequence options[] = new CharSequence[]{"Open Profile", "Send message"};

                                                        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                        builder.setItems(options, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, int i) {

                                                                //Click Event for each item.
                                                                if (i == 0) {

                                                                    Intent profileIntent = new Intent(FriendsFragment.this.getContext(), ProfileActivity.class);
                                                                    profileIntent.putExtra("user_id", userId);
                                                                    startActivity(profileIntent);

                                                                }

                                                                if (i == 1) {

                                                                    Intent chatIntent = new Intent(FriendsFragment.this.getContext(), ChatActivity.class);
                                                                    chatIntent.putExtra("user_id", userId);
                                                                    chatIntent.putExtra("user_name", userName);
                                                                    startActivity(chatIntent);

                                                                }

                                                            }
                                                        });

                                                        builder.show();

                                                    }
                                                });
                                            }

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

                }
            };

            mFriendsList.setAdapter(firebaseAdapter);
        }
        else
        {
            mFriendsList.setAdapter(friendsRecyclerViewAdapter);
        }
    }



    @Override
    public void onStart() {
        super.onStart();

        friendsRecyclerViewAdapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(

                Friends.class,
                R.layout.users_single_layout,
                FriendsViewHolder.class,
                mFriendsDatabase


        ) {
            @Override
            protected void populateViewHolder(final FriendsViewHolder friendsViewHolder, Friends friends, int i) {

                friendsViewHolder.setDate(friends.getDate());

                final String list_user_id = getRef(i).getKey();

                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        noFriends.setVisibility(View.INVISIBLE);

                        if(dataSnapshot.hasChild("name")) {

                            final String userName = dataSnapshot.child("name").getValue().toString();
                            String userThumb = dataSnapshot.child("thumb_image").getValue().toString();

                            if (dataSnapshot.hasChild("online")) {

                                String userOnline = dataSnapshot.child("online").getValue().toString();
                                friendsViewHolder.setUserOnline(userOnline);

                            }

                            friendsViewHolder.setName(userName);
                            friendsViewHolder.setUserImage(userThumb);

                            friendsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    CharSequence options[] = new CharSequence[]{"Open Profile", "Send message"};

                                    final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                    builder.setItems(options, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {

                                            //Click Event for each item.
                                            if (i == 0) {

                                                Intent profileIntent = new Intent(FriendsFragment.this.getContext(), ProfileActivity.class);
                                                profileIntent.putExtra("user_id", list_user_id);
                                                startActivity(profileIntent);

                                            }

                                            if (i == 1) {

                                                Intent chatIntent = new Intent(FriendsFragment.this.getContext(), ChatActivity.class);
                                                chatIntent.putExtra("user_id", list_user_id);
                                                chatIntent.putExtra("user_name", userName);
                                                startActivity(chatIntent);

                                            }

                                        }
                                    });

                                    builder.show();

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

        mFriendsList.setAdapter(friendsRecyclerViewAdapter);


    }


    public static class FriendsViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public FriendsViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

        }

        public void setDate(String date){

            TextView userStatusView =  mView.findViewById(R.id.prof_status);
            userStatusView.setText(date);

        }

        public void setName(String name){

            TextView userNameView =  mView.findViewById(R.id.prof_name);
            userNameView.setText(name);

        }

        public void setUserImage(String thumb_image){

            CircleImageView userImage = mView.findViewById(R.id.prof_img);
            Picasso.get().load(thumb_image).placeholder(R.drawable.default_contact).into(userImage);

        }

        public void setUserOnline(String online_status) {

            ImageView userOnlineView =  mView.findViewById(R.id.prof_online_icon);

            if(online_status.equals("true")){

                userOnlineView.setVisibility(View.VISIBLE);

            } else {

                userOnlineView.setVisibility(View.INVISIBLE);

            }

        }


    }


}