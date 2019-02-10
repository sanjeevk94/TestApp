package com.scubearena.testapp;

import android.app.Activity;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;


public class NotificationSettings extends AppCompatActivity {

    Toolbar toolbar;
    ArrayList<PrivacyItem> notificationItemList=new ArrayList<>();
    ListView ntf_list;

    ArrayAdapter adapter;

    DatabaseReference userRef;
    String notif_tone_status="N";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_settings);

        toolbar = findViewById(R.id.notification_appBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Notification Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ntf_list = findViewById(R.id.notification_listview);

        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                notificationItemList=new ArrayList<>();
                String notification_tone = (String)dataSnapshot.child("notification_tone").getValue();
                String notifcTone = (String) dataSnapshot.child("tone").getValue();
                String mute = (String)dataSnapshot.child("mute").getValue();
                String vibrate = (String)dataSnapshot.child("vibrate").getValue();
                if(notifcTone.equals("Y")) {
                    notif_tone_status ="Y";
                    notificationItemList.add(new PrivacyItem("Notification Tone", true));
                    if(notification_tone.equals(""))
                    selectNotificationTone();
                }
                else
                    notificationItemList.add(new PrivacyItem("Notification Tone",false));

                if(mute.equals("Y"))
                    notificationItemList.add(new PrivacyItem("Mute",true));
                else
                    notificationItemList.add(new PrivacyItem("Mute",false));

                if(vibrate.equals("Y"))
                    notificationItemList.add(new PrivacyItem("Vibrate",true));
                else
                    notificationItemList.add(new PrivacyItem("Vibrate",false));

                final NotificationCustomAdapter adapter=new NotificationCustomAdapter(notificationItemList,NotificationSettings.this);
                ntf_list.setAdapter(adapter);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void selectNotificationTone()
    {

        Uri currentUri = RingtoneManager.getActualDefaultRingtoneUri(NotificationSettings.this,RingtoneManager.TYPE_NOTIFICATION);
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select ringtone for notifications:");
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,RingtoneManager.TYPE_NOTIFICATION);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, currentUri);
        startActivityForResult(intent, 5);

    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent intent)
    {
        if (resultCode == Activity.RESULT_OK && requestCode == 5)
        {
            Uri uri = intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);

            if (uri != null)
            {
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                Map updateHashMap = new HashMap<>();
                updateHashMap.put("notification_tone",uri.toString());
                userRef.updateChildren(updateHashMap).addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful())
                        {
                            Toasty.info(NotificationSettings.this,"Notiification tone Changed",Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
            else
            {

            }
        }
    }
}
