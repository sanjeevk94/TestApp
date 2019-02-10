package com.scubearena.testapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hootsuite.nachos.ChipConfiguration;
import com.hootsuite.nachos.NachoTextView;
import com.hootsuite.nachos.chip.Chip;
import com.hootsuite.nachos.chip.ChipSpan;
import com.hootsuite.nachos.chip.ChipSpanChipCreator;
import com.hootsuite.nachos.terminator.ChipTerminatorHandler;
import com.hootsuite.nachos.tokenizer.SpanChipTokenizer;
import com.hootsuite.nachos.validator.ChipifyingNachoValidator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class InterestActivity extends AppCompatActivity {

    private static String TAG = "Nachos";
    private static String[] SUGGESTIONS = new String[]{"Movies","Travelling","Food","Shopping","Reading"};

    @BindView(R.id.nacho_text_view_with_icons)
    NachoTextView mNachoTextViewWithIcons;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interests);
        ButterKnife.bind(this);

        setupChipTextView(mNachoTextViewWithIcons);


        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = currentUser.getUid();
        DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Interests").child(uid);
        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("Interests")) {
                    String interests = dataSnapshot.child("Interests").getValue().toString();
                    setDefaultChips(interests);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        mNachoTextViewWithIcons.setChipTokenizer(new SpanChipTokenizer<>(this, new ChipSpanChipCreator() {
            @Override
            public ChipSpan createChip(@NonNull Context context, @NonNull CharSequence text, Object data) {
                switch(text.toString())
                {
                    case "Movies":
                        return new ChipSpan(context, text, ContextCompat.getDrawable(InterestActivity.this,R.drawable.ic_local_movies_black_24dp), data);
                    case "Travelling":
                        return new ChipSpan(context, text, ContextCompat.getDrawable(InterestActivity.this, R.drawable.ic_flight_takeoff_black_24dp), data);
                    case "Food":
                        return new ChipSpan(context, text, ContextCompat.getDrawable(InterestActivity.this, R.drawable.ic_local_pizza_black_24dp), data);
                    case "Shopping":
                        return new ChipSpan(context, text, ContextCompat.getDrawable(InterestActivity.this, R.drawable.ic_local_mall_black_24dp), data);
                    case "Reading":
                        return new ChipSpan(context, text, ContextCompat.getDrawable(InterestActivity.this, R.drawable.ic_library_books_black_24dp), data);
                }
                return new ChipSpan(context, text, ContextCompat.getDrawable(InterestActivity.this, R.mipmap.ic_launcher), data);
            }

            @Override
            public void configureChip(@NonNull ChipSpan chip, @NonNull ChipConfiguration chipConfiguration) {
                super.configureChip(chip, chipConfiguration);
            }
        }, ChipSpan.class));
    }

    private void setupChipTextView(NachoTextView nachoTextView) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, SUGGESTIONS);
        nachoTextView.setAdapter(adapter);
        nachoTextView.setIllegalCharacters();

        nachoTextView.addChipTerminator('\n', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_ALL);
        nachoTextView.addChipTerminator(' ', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_TO_TERMINATOR);
        nachoTextView.addChipTerminator(';', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_CURRENT_TOKEN);
        nachoTextView.setNachoValidator(new ChipifyingNachoValidator());
        nachoTextView.enableEditChipOnTouch(true, true);
        nachoTextView.setOnChipClickListener(new NachoTextView.OnChipClickListener() {
            @Override
            public void onChipClick(Chip chip, MotionEvent motionEvent) {
                Log.d(TAG, "onChipClick: " + chip.getText());
            }
        });
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.list_chip_values)
    public void listChipValues(View view) {
        List<String> chipValues = mNachoTextViewWithIcons.getChipValues();
        captureInterests(chipValues);
        }

    private void alertStringList(String title, List<String> list) {
        String alertBody;
        if (!list.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            for (String chipValue : list) {
                builder.append(chipValue);
                builder.append("\n");
            }
            builder.deleteCharAt(builder.length() - 1);
            alertBody = builder.toString();
        } else {
            alertBody = "No strings";
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(alertBody)
                .setCancelable(true)
                .setNegativeButton("Close", null)
                .create();

        dialog.show();
    }
    private void captureInterests(final List interests)
    {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = currentUser.getUid();
        DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Interests").child(uid);
        HashMap<String,String> userMap = new HashMap<>();
        userMap.put("Interests",interests.toString());
        mDatabaseReference.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    alertStringList("Your interests are saved", interests);

                }
                else
                {
                    alertStringList("Your interests failed to save", interests);

                }
            }
        });
    }
    private void setDefaultChips(String interests)
    {
        List<String>testList = new ArrayList<>();
        String[] list = interests.split(",");
        for (String s : list)
        {
            if(s.contains("["))
            {
                s=s.replace("[","");

            }
            else if(s.contains("]"))
            {
               s=s.replace("]","");

            }
            testList.add(s);
        }

        mNachoTextViewWithIcons.setText(testList);


    }
}