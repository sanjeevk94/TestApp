package com.scubearena.testapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import es.dmoral.toasty.Toasty;

public class PrivacySettings extends AppCompatActivity {

    ListView privacyItems;

    ArrayList<PrivacyItem> myitems=new ArrayList<>();
    Toolbar toolbar;

    DatabaseReference userRef;

    String profilePicStatus;
    String profileStatus;
    String profileContactDtlsStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_settings);
        toolbar = findViewById(R.id.privacy_appBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Privacy Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        privacyItems=findViewById(R.id.privacy_listview);
        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                myitems=new ArrayList<>();
                profilePicStatus = (String) dataSnapshot.child("profile_pic_hide").getValue();
                profileStatus = (String)dataSnapshot.child("profile_status_hide").getValue();
                profileContactDtlsStatus = (String)dataSnapshot.child("profile_contact_details").getValue();
                if(profilePicStatus.equals("Y"))
                    myitems.add(new PrivacyItem("Hide your profile picture",true));
                else
                    myitems.add(new PrivacyItem("Hide your profile picture",false));

                if(profileStatus.equals("Y"))
                    myitems.add(new PrivacyItem("Hide your status",true));
                else
                    myitems.add(new PrivacyItem("Hide your status",false));

                if(profileContactDtlsStatus.equals("Y"))
                    myitems.add(new PrivacyItem("Hide your contact details",true));
                else
                    myitems.add(new PrivacyItem("Hide your contact details",false));

                final PrivacyCustomAdapter adapter=new PrivacyCustomAdapter(myitems,PrivacySettings.this);
                privacyItems.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}
