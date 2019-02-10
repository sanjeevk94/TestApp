package com.scubearena.testapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
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

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;


public class EventActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private TextInputLayout mEventName;
    private TextInputLayout mEventDate;
    private TextInputEditText meditText;
    private AutoCompleteTextView autoTextView;
    private Button eventSave;
    private DatePickerDialog datePicker;
    private int saveFlag=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

        mToolbar = findViewById(R.id.addEvents_appBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Lock the event");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mEventName = findViewById(R.id.event_name);
        mEventDate = findViewById(R.id.event_date);
        eventSave = findViewById(R.id.add_btn);

        meditText = findViewById(R.id.date);
        autoTextView = findViewById(R.id.autotextview);

        final String editEventName = getIntent().getStringExtra("eventName");
        final String editEventDate = getIntent().getStringExtra("eventDate");
        if(editEventName != null && editEventDate != null) {
            mEventName.getEditText().setText(editEventName);
            mEventDate.getEditText().setText(editEventDate);
            
            saveFlag = 1;
        }

        String[] events = getResources().getStringArray(R.array.events_array);
        ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,events);
        autoTextView.setAdapter(adapter);

        meditText.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int month = calendar.get(Calendar.MONTH);
                int year = calendar.get(Calendar.YEAR);
                final DecimalFormat mFormat= new DecimalFormat("00");

                System.out.println("day+month+year"+day+" "+month+" "+year);
                datePicker = new DatePickerDialog(EventActivity.this, android.R.style.Theme_Holo_Light_Dialog,new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth)
                    {
                        String date =  mFormat.format(Double.valueOf(dayOfMonth)) + "/" +  mFormat.format(Double.valueOf(month+1)) + "/" +  mFormat.format(Double.valueOf(year));
                        mEventDate.getEditText().setText(date);
                    }
                },year,month,day);
                datePicker.setIcon(R.drawable.ic_date_range_black_24dp);
                datePicker.setTitle("Lock event date");
            datePicker.show();
            }
        });

        eventSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String eventName = mEventName.getEditText().getText().toString();
                String eventDate = mEventDate.getEditText().getText().toString();
                SimpleDateFormat dateFormat= new SimpleDateFormat("dd/MM/yyyy");
                try {
                    eventDate = dateFormat.format(dateFormat.parse(eventDate).toString());
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
                Toast.makeText(EventActivity.this,eventDate,Toast.LENGTH_LONG).show();
                if(saveFlag == 1)
                {
                    editEvent(eventName,eventDate);
                }
                else
                {
                    saveEvent(eventName, eventDate);
                }
            }
        });

    }

    private void saveEvent(String eName, String eDate)
    {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = currentUser.getUid();
        DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference mPushDatabaseReference = mRootRef.child("Events").child(uid).push();
        String pushId = mPushDatabaseReference.getKey();

        Map eventMap = new HashMap();
        eventMap.put("title",eName);
        eventMap.put("date",eDate);

        Map eventUserMap = new HashMap();
        eventUserMap.put("Events/"+uid+"/"+pushId,eventMap);
        mRootRef.updateChildren(eventUserMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                mEventName.getEditText().setText("");
                mEventDate.getEditText().setText("");
                Toasty.success(EventActivity.this, "Event added", Toast.LENGTH_SHORT).show();
                /*Intent eventlist = new Intent(EventActivity.this,EventListActivity.class);
                startActivity(eventlist);*/
            }
        });
    }

    private void editEvent(final String eName, final String eDate)
    {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = currentUser.getUid();
        DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
        final DatabaseReference mEventsDatabaseRef = mRootRef.child("Events").child(uid);
        mEventsDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> fbEventList = dataSnapshot.getChildren();
                for(DataSnapshot eventNode : fbEventList)
                {
                    String eventName = eventNode.child("title").getValue().toString();
                    if(eventName.equals(eName))
                    {
                        System.out.println("eventNode.getKey()"+eventNode.getKey());
                        mEventsDatabaseRef.child(eventNode.getKey()).child("date").setValue(eDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                mEventName.getEditText().setText("");
                                mEventDate.getEditText().setText("");
                                saveFlag =0;
                                Toasty.success(EventActivity.this,"Event edited",Toast.LENGTH_SHORT).show();
                                /*Intent eventlist = new Intent(EventActivity.this,EditEventActivity.class);
                                startActivity(eventlist);
                                finish();*/
                            }
                        });
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toasty.error(EventActivity.this,"Event edit failed",Toast.LENGTH_LONG).show();
            }
        });
    }
}


                        //mEventDate.getEditText().setText(dayOfMonth + "/" + (month+1) + "/" + year);
