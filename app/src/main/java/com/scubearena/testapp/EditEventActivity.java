package com.scubearena.testapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.view.Menu;

import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class EditEventActivity extends AppCompatActivity {

    private DatabaseReference mEventsDatabaseRef;
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private String mCurrentUserId;

    private FirebaseRecyclerAdapter<Event, EditEventViewHolder> firebaseEditEventAdapter;
    private static RecyclerView recyclerView;
    private static ArrayList<DataModel> data;
    private RecyclerView.LayoutManager layoutManager;
    static View.OnClickListener myOnClickListener;
    private Toolbar mToolbar;
    private TextView noEvents;
    int i =0;
    int itemPos = 0;
    private ActionMode actionMode;
    private final List<Event> eventList = new ArrayList<>();
    private String title;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editevent);

        myOnClickListener = new MyOnClickListener(this);

        mToolbar = findViewById(R.id.editevents_appBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Events");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = findViewById(R.id.event_recycler);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        noEvents = findViewById(R.id.emptyeventsView);


        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        mCurrentUserId = mCurrentUser.getUid();
        mEventsDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Events").child(mCurrentUserId);
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {

            }

            @Override
            public void onLongClick(View view, int position) {
                itemPos = position;
                Toast.makeText(EditEventActivity.this,"Selected Item "+itemPos,Toast.LENGTH_LONG).show();
                actionMode = EditEventActivity.this.startActionMode(new ActionMode.Callback() {
                    @Override
                    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                        MenuInflater inflater = mode.getMenuInflater();
                        inflater.inflate(R.menu.event_menu, menu);
                        return true;
                    }

                    @Override
                    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                        return false;
                    }

                    @Override
                    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.edit:
                                editEvent();
                                actionMode.finish();
                                return true;
                            case R.id.delete:
                                deleteEvent();
                                actionMode.finish();
                                return true;
                        }
                        return false;
                    }

                    @Override
                    public void onDestroyActionMode(ActionMode mode) {

                    }
                });

                }
        }));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    private  class MyOnClickListener implements View.OnClickListener {

        private final Context context;

        private MyOnClickListener(Context context) {
            this.context = context;
        }

        @Override
        public void onClick(View v) {
            removeItem(v);
        }

        private void removeItem(View v) {
            int selectedItemPosition = recyclerView.getChildPosition(v);
            RecyclerView.ViewHolder viewHolder
                    = recyclerView.findViewHolderForPosition(selectedItemPosition);
            final TextView eventName
                    =  viewHolder.itemView.findViewById(R.id.eventName);
            final TextView eventDate
                    =  viewHolder.itemView.findViewById(R.id.eventDate);
            /*ImageView eventEdit = viewHolder.itemView.findViewById(R.id.event_edit);
            ImageView eventDelete = viewHolder.itemView.findViewById(R.id.event_delete);*/

            final String selectedName = (String) eventName.getText();
            final String selectedDate = (String) eventDate.getText();
            /*eventEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent editIntent = new Intent(EditEventActivity.this,EventActivity.class);
                    editIntent.putExtra("eventName",selectedName);
                    editIntent.putExtra("eventDate",selectedDate);
                    startActivity(editIntent);
                    finish();
                }
            });
            eventDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    open(v,selectedName);
                }
            });*/
            int selectedItemId = -1;
            for (int i = 0; i < MyData.eventNameArray.length; i++) {
                if (selectedName.equals(MyData.eventNameArray[i])) {
                    selectedItemId = MyData.id_[i];
                }
            }
        }
    }

    private void open(View view, final String eventName){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Are you sure, You wanted to delete event");
                alertDialogBuilder.setPositiveButton("yes",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                               // deleteEvent(eventName);
                            }
                        });

        alertDialogBuilder.setNegativeButton("No",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void deleteEvent()
    {
        title = eventList.get(itemPos).getTitle();
        mEventsDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> fbEventList = dataSnapshot.getChildren();
                for(DataSnapshot eventNode : fbEventList)
                {
                    String eventName = eventNode.child("title").getValue().toString();
                    if(eventName.equals(title))
                    {
                        eventNode.getRef().removeValue();
                        title ="";
                        Toasty.success(EditEventActivity.this,"Event deleted",Toast.LENGTH_LONG).show();
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toasty.error(EditEventActivity.this,"Event deletion failed",Toast.LENGTH_LONG).show();
            }
        });

    }

    private void editEvent()
    {
        Intent event = new Intent(EditEventActivity.this,EventActivity.class);
        event.putExtra("eventName",eventList.get(itemPos).getTitle());
        event.putExtra("eventDate",eventList.get(itemPos).getDate());
        startActivity(event);
    }

    public void onStart()
    {
        super.onStart();
        firebaseEditEventAdapter = new FirebaseRecyclerAdapter<Event, EditEventViewHolder>(
                Event.class,
                R.layout.event_cardlayout,
                EditEventViewHolder.class,
                mEventsDatabaseRef

        ) {
            @Override
            protected void populateViewHolder(final EditEventViewHolder viewHolder, Event model, int position)
            {
                final String list_user_id = getRef(position).getKey();
                System.out.println("list_user_id"+list_user_id);
                Query lastMessageQuery = mEventsDatabaseRef.child(list_user_id);
                lastMessageQuery.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        if(dataSnapshot.hasChild("title")) {
                            noEvents.setVisibility(View.INVISIBLE);
                            Event event = dataSnapshot.getValue(Event.class);
                            String name = dataSnapshot.child("title").getValue().toString();
                            String date = dataSnapshot.child("date").getValue().toString();
                            eventList.add(i++, event);
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
        recyclerView.setAdapter(firebaseEditEventAdapter);

    }

    public static class EditEventViewHolder extends RecyclerView.ViewHolder {

        View mView;
        public EditEventViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setEventName(String eventName){

            TextView userStatusView =  mView.findViewById(R.id.eventName);
            userStatusView.setText(eventName);


        }

        public void setEventDate(String eventDate){

            TextView userNameView =  mView.findViewById(R.id.eventDate);
            userNameView.setText(eventDate);

        }

        public void setEventImage(String eventName){

            ImageView userImageView =  mView.findViewById(R.id.imageView);
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
