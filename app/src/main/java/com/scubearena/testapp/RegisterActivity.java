package com.scubearena.testapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Random;

import es.dmoral.toasty.Toasty;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout mdisplayName;
    private TextInputLayout memail;
    private TextInputLayout mpassword;
    private Button createButton;
    private FirebaseAuth mAuth;
    private TextView textView;
    private ProgressDialog mProgressDialog;
    private DatabaseReference mDatabaseReference;
    private AwesomeValidation awesomeValidation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();
        mdisplayName =  findViewById(R.id.reg_disname);
        memail =  findViewById(R.id.reg_email);
        mpassword =  findViewById(R.id.reg_password);
        createButton =  findViewById(R.id.reg_createbtn);
        textView =  findViewById(R.id.link_login);
        mProgressDialog = new ProgressDialog(this,R.style.MyAlertDialogStyle);
        awesomeValidation = new AwesomeValidation(ValidationStyle.BASIC);


        awesomeValidation.addValidation(this, R.id.name, "^(?=.*[a-zA-Z].*)[a-zA-Z\\d!@#$%&* ]{4,}$", R.string.nameerror);
        awesomeValidation.addValidation(this, R.id.email, Patterns.EMAIL_ADDRESS, R.string.emailerror);
        awesomeValidation.addValidation(this, R.id.password, "^(?=.*[a-zA-Z\\d].*)[a-zA-Z\\d!@#$%&*]{7,}$", R.string.pwderror);

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginIntent = new Intent(RegisterActivity.this,LoginActivity.class);
                startActivity(loginIntent);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);

            }
        });

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String displayName = mdisplayName.getEditText().getText().toString();
                String email = memail.getEditText().getText().toString();
                String password = mpassword.getEditText().getText().toString();
                if(validate()){
                mProgressDialog.setMessage("Please wait while we create your account");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();
                registerUser(displayName,email,password);
                }
            }
        });
    }

    public void registerUser(final String displayName, final String email, String password)
    {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            FirebaseUser currentUser =FirebaseAuth.getInstance().getCurrentUser();
                            String uid = currentUser.getUid();
                            mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

                            String deviceToken = FirebaseInstanceId.getInstance().getToken();
                            HashMap<String,String> userMap = new HashMap<>();

                            userMap.put("device_token",deviceToken);
                            userMap.put("id",Integer.toString(UniqueId()));
                            userMap.put("email",email);
                            userMap.put("name",displayName);
                            userMap.put("status","Hi there I'm Using Gift chat app");
                            userMap.put("image","default");
                            userMap.put("thumb_image","default");
                            userMap.put("chat_bg_image","default");
                            userMap.put("chat_bg_thumb_image","default");
                            userMap.put("profile_pic_hide","N");
                            userMap.put("profile_status_hide","N");
                            userMap.put("profile_contact_details","N");
                            userMap.put("tone","Y");
                            userMap.put("mute","N");
                            userMap.put("vibrate","N");
                            userMap.put("default_msg","Hello buddy you there...");
                            userMap.put("notification_tone","content://media/internal/audio/media/46");

                            mDatabaseReference.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful())
                                    {
                                        mProgressDialog.dismiss();
                                        Toasty.success(RegisterActivity.this,"Registration Success",Toast.LENGTH_LONG).show();
                                        Intent mainIntent = new Intent(RegisterActivity.this,MainActivity.class);
                                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(mainIntent);
                                        finish();
                                    }
                                }
                            });
                        } else {
                            mProgressDialog.hide();
                            Toasty.error(RegisterActivity.this, "Registration failed.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public int UniqueId()
    {
        Random rand = new Random();

         int n = rand.nextInt(50) + 1;
        return n;
    }

    public boolean validate() {
        boolean valid = false;

        if(awesomeValidation.validate())
        {
            valid = true;
        }

        /*String name = mdisplayName.getEditText().getText().toString();
        String email = memail.getEditText().getText().toString();
        String password = mpassword.getEditText().getText().toString();

        if (name.isEmpty() || name.length() < 3) {
            mdisplayName.setError("at least 3 characters");
            valid = false;
        } else {
            mdisplayName.setError(null);
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            memail.setError("enter a valid email address");
            valid = false;
        } else {
            memail.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            mpassword.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            mpassword.setError(null);
        }*/

        return valid;
    }

}
