package com.scubearena.testapp;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import es.dmoral.toasty.Toasty;


public class StatusActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private TextInputLayout textInputLayout;
    private Button button;
    private DatabaseReference mStatusDbReference;
    private FirebaseUser mCurrentUser;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        textInputLayout = findViewById(R.id.status_edittext);
        button = findViewById(R.id.status_btn);
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = mCurrentUser.getUid();
        mStatusDbReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        mToolbar = findViewById(R.id.status_appBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        String statusValue = getIntent().getStringExtra("statusValue");
        textInputLayout.getEditText().setText(statusValue);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressDialog = new ProgressDialog(StatusActivity.this,R.style.MyAlertDialogStyle);
                mProgressDialog.setMessage("Please wait while we save the changes");
                mProgressDialog.show();
                String status = textInputLayout.getEditText().getText().toString();
                mStatusDbReference.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            mProgressDialog.dismiss();
                            Toasty.success(StatusActivity.this,"Status update success",Toast.LENGTH_LONG).show();
                        }
                        else
                        {
                            Toasty.error(StatusActivity.this,"There was an error in saving changes",Toast.LENGTH_LONG).show();
                        }
                    }
                });


            }
        });



    }
}
