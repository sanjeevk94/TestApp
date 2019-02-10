package com.scubearena.testapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class EventListActivity extends AppCompatActivity
{

    private DatabaseReference mEventsDatabaseRef;
    private FirebaseRecyclerAdapter<Event, EventViewHolder> firebaseEventAdapter;
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private String mCurrentUserId;
    private RecyclerView meventList;
    private Toolbar mToolbar;
    private TextView noEvents;
    int i =0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);
        mToolbar = findViewById(R.id.events_appBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("My events");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        noEvents = findViewById(R.id.emptyevents);
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        mCurrentUserId = mCurrentUser.getUid();
        mEventsDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Events").child(mCurrentUserId);
        meventList = findViewById(R.id.list);
        meventList.setHasFixedSize(true);
        meventList.setLayoutManager(new LinearLayoutManager(this));
    }

    public void onStart() {
        super.onStart();
        firebaseEventAdapter = new FirebaseRecyclerAdapter<Event, EventViewHolder>(
                Event.class,
                R.layout.custom_listview,
                EventViewHolder.class,
                mEventsDatabaseRef

        ) {
            @Override
            protected void populateViewHolder(final EventViewHolder viewHolder, Event model, int position)
            {
                final String list_user_id = getRef(position).getKey();
                System.out.println("list_user_id"+list_user_id);
                Query lastMessageQuery = mEventsDatabaseRef.child(list_user_id);
                lastMessageQuery.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild("title")) {
                            noEvents.setVisibility(View.INVISIBLE);
                            String name = dataSnapshot.child("title").getValue().toString();
                            String date = dataSnapshot.child("date").getValue().toString();
                            viewHolder.setEventName(name);
                            viewHolder.setEventDate(date);
                            viewHolder.setEventImage(name);
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };
        meventList.setAdapter(firebaseEventAdapter);

    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public EventViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

        }

        public void setEventName(String eventName){

            TextView userStatusView =  mView.findViewById(R.id.title);
            userStatusView.setText(eventName);


        }

        public void setEventDate(String eventDate){

            TextView userNameView =  mView.findViewById(R.id.date);
            userNameView.setText(eventDate);

        }

        public void setEventImage(String eventName){

            ImageView userImageView =  mView.findViewById(R.id.icon);
            if(eventName.contains("Birthday"))
            {
                userImageView.setImageResource(R.drawable.ic_event_bd);
            }
            else if(eventName.contains("Marriage"))
            {
                userImageView.setImageResource(R.drawable.ic_event_ma);
            }
            else if(eventName.contains("House"))
            {
                userImageView.setImageResource(R.drawable.ic_event_hw);
            }
        }
    }



}
