package com.scubearena.testapp;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import es.dmoral.toasty.Toasty;

public class DefaultMessage extends AppCompatActivity {

    private Toolbar mToolbar;
    private TextInputLayout textInputLayout;
    private Button button;
    private DatabaseReference mDefMsgDbReference;
    private FirebaseUser mCurrentUser;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default_message);

        textInputLayout = findViewById(R.id.msg_edittext);
        button = findViewById(R.id.change_msg_btn);
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = mCurrentUser.getUid();
        mDefMsgDbReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        mToolbar = findViewById(R.id.message_appBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Default Message");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mDefMsgDbReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String defMsg = dataSnapshot.child("default_msg").getValue().toString();
                textInputLayout.getEditText().setText(defMsg);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressDialog = new ProgressDialog(DefaultMessage.this,R.style.MyAlertDialogStyle);
                mProgressDialog.setMessage("Please wait while we save the changes");
                mProgressDialog.show();
                String status = textInputLayout.getEditText().getText().toString();
                mDefMsgDbReference.child("default_msg").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            mProgressDialog.dismiss();
                            Toasty.success(DefaultMessage.this,"Message Saved",Toast.LENGTH_LONG).show();
                        }
                        else
                        {
                            Toasty.error(DefaultMessage.this,"There was an error in saving changes",Toast.LENGTH_LONG).show();
                        }
                    }
                });


            }
        });
    }
}
