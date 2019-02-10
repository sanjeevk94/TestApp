package com.scubearena.testapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.dmoral.toasty.Toasty;

public class AccountSettingsActivity extends AppCompatActivity {
    ListView listItems;
    String[] listContents = {"Profile", "Privacy","Notifications","Chats","Intrests"};
    int[] listviewImage = new int[]{
            R.drawable.ic_account_box_24dp, R.drawable.ic_lock_24dp, R.drawable.ic_alert_24dp, R.drawable.ic_chat_black_24dp,R.drawable.ic_star_black_24dp};
    String[] listviewShortDescription = new String[]{
            "Change image and status", "change account visibility", "change notification tone", "change chat settings","your interests"};


    RelativeLayout chatLayout;
    ArrayAdapter<String> adapter;

    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acctsett);

        toolbar = findViewById(R.id.actset_appBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Account Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        chatLayout = findViewById(R.id.chatLayout);

        listItems = findViewById(R.id.listItems);

        List<HashMap<String, String>> aList = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            HashMap<String, String> hm = new HashMap<>();
            hm.put("listview_title", listContents[i]);
            hm.put("listview_discription", listviewShortDescription[i]);
            hm.put("listview_image", Integer.toString(listviewImage[i]));
            aList.add(hm);
        }

        String[] from = {"listview_image", "listview_title", "listview_discription"};
        int[] to = {R.id.listview_image, R.id.listview_item_title, R.id.listview_item_short_description};

        SimpleAdapter simpleAdapter = new SimpleAdapter(getBaseContext(), aList, R.layout.acct_custome_listview, from, to);
        listItems.setAdapter(simpleAdapter);

        /*adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1, listContents);
        listItems.setAdapter(adapter);*/
        listItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switchActivity(position);
            }
        });


    }

    private void switchActivity(int position) {
        Intent changeIntent;
        switch (position) {
            case 0:
                changeIntent = new Intent(AccountSettingsActivity.this, SettingsActivity.class);
                startActivity(changeIntent);
                break;

            case 1:
                changeIntent = new Intent(AccountSettingsActivity.this,PrivacySettings.class);
                startActivity(changeIntent);
                break;
            case 2:
                /*changeIntent = new Intent(AccountSettingsActivity.this,NotificationSettings.class);
                startActivity(changeIntent);*/
                selectNotificationTone();
                break;

            case 3:
                changeIntent = new Intent(AccountSettingsActivity.this,DefaultMessage.class);
                startActivity(changeIntent);
                break;
            case 4:
                changeIntent = new Intent(AccountSettingsActivity.this,InterestActivity.class);
                startActivity(changeIntent);
                break;

        }
    }

    public void selectNotificationTone()
    {

        Uri currentUri = RingtoneManager.getActualDefaultRingtoneUri(AccountSettingsActivity.this,RingtoneManager.TYPE_NOTIFICATION);
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
                            Toasty.info(AccountSettingsActivity.this,"Notiification tone Changed",Toast.LENGTH_LONG).show();
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