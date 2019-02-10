package com.scubearena.testapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sa90.materialarcmenu.ArcMenu;
import com.sa90.materialarcmenu.StateChangeListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

public class EventsFragment extends Fragment {

    private View mMainView;

    private TextView textView;

    private ArcMenu arcMenuAndroid;

    private FirebaseRecyclerAdapter<Event,UsersActivity.UsersViewHolder> firebaseRecyclerAdapter;

    private DatabaseReference mUserEventDatabaseRef,mUserDatabaseRef;

    private FirebaseAuth mAuth;

    private String currentUser;

    private RecyclerView mFriendsEventList;

    private String eventTitle="";

    private String currentDate;

    private String userName;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mMainView = inflater.inflate(R.layout.fragment_events, container, false);
        textView = mMainView.findViewById(R.id.no_events);
        arcMenuAndroid =  mMainView.findViewById(R.id.event_log);
        mFriendsEventList = mMainView.findViewById(R.id.friends_event_list);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser().getUid();

        Date date = Calendar.getInstance().getTime();
        System.out.println("Current time => " + date);
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        currentDate = sdf.format(date);
        System.out.println("Today : "+sdf.format(date));
        mUserEventDatabaseRef = FirebaseDatabase.getInstance().getReference().child("EventUserInfo").child(currentDate).child(currentUser);
        mUserEventDatabaseRef.keepSynced(true);

        mUserDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mUserDatabaseRef.keepSynced(true);

        mFriendsEventList.setHasFixedSize(true);
        mFriendsEventList.setLayoutManager(new LinearLayoutManager(getContext()));

        arcMenuAndroid.setStateChangeListener(new StateChangeListener() {
            @Override
            public void onMenuOpened() {
                FloatingActionButton fab1 = mMainView.findViewById(R.id.add_event);
                fab1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent startIntent = new Intent(getActivity(),EventActivity.class);
                        startActivity(startIntent);

                    }
                });
                FloatingActionButton fab2 = mMainView.findViewById(R.id.list_event);
                fab2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent startIntent = new Intent(getActivity(),EventListActivity.class);
                        startActivity(startIntent);

                    }
                });

                FloatingActionButton fab3 = mMainView.findViewById(R.id.edit_event);
                fab3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent startIntent = new Intent(getActivity(),EditEventActivity.class);
                        startActivity(startIntent);

                    }
                });

            }
            @Override
            public void onMenuClosed() {

            }
        });


        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Event, UsersActivity.UsersViewHolder>(
                Event.class,
                R.layout.users_single_layout,
                UsersActivity.UsersViewHolder.class,
                mUserEventDatabaseRef
        )  {
            @Override
            protected void populateViewHolder(final UsersActivity.UsersViewHolder viewHolder, final Event Event, int position) {

                final String list_user_id = getRef(position).getKey();

                mUserDatabaseRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        textView.setVisibility(View.INVISIBLE);
                        userName = dataSnapshot.child("name").getValue().toString();
                        String thumbImage = dataSnapshot.child("thumb_image").getValue().toString();
                        viewHolder.setName(userName);
                        viewHolder.setImage(thumbImage);
                        DatabaseReference eventNamesRef = FirebaseDatabase.getInstance().getReference().child("EventUserInfo").child(currentDate).child(currentUser).child(list_user_id);
                        eventNamesRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Iterable<DataSnapshot> totalChildren = dataSnapshot.getChildren();
                                String eventName="";
                                for(DataSnapshot snapshot : totalChildren)
                                {
                                    if(currentDate.equals(snapshot.child("date").getValue().toString()))
                                    {
                                        eventName = eventName+snapshot.child("title").getValue().toString();
                                    }
                                }
                                viewHolder.setStatus("Your friend has "+eventName+" today. Send your wishes with gift.");
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

                final String userId = getRef(position).getKey();
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        Intent OffersIntent = new Intent(EventsFragment.this.getContext(),OffersActivity.class);
                        OffersIntent.putExtra("user_name",userName);
                        OffersIntent.putExtra("user_id",list_user_id);
                        startActivity(OffersIntent);
                    }
                });
            }
        };
        mFriendsEventList.setAdapter(firebaseRecyclerAdapter);
    }
}
