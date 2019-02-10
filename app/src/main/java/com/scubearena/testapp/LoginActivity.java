package com.scubearena.testapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import es.dmoral.toasty.Toasty;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout memail;
    private TextInputLayout mpassword;
    private EditText emailValue;
    private EditText passwordValue;
    private Button loginButton;
    private FirebaseAuth mAuth;
    private TextView textView;
    private ProgressDialog mProgressDailog;
    private DatabaseReference mUserDatabase;
    private AwesomeValidation awesomeValidation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        memail =  findViewById(R.id.log_email);
        mpassword =  findViewById(R.id.log_password);
        emailValue = findViewById(R.id.email);
        passwordValue = findViewById(R.id.password);
        loginButton =  findViewById(R.id.log_btn);
        textView =  findViewById(R.id.link_signup);
        mProgressDailog = new ProgressDialog(this,R.style.MyAlertDialogStyle);
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        awesomeValidation = new AwesomeValidation(ValidationStyle.BASIC);

        awesomeValidation.addValidation(this, R.id.email, Patterns.EMAIL_ADDRESS, R.string.emailerror);
        awesomeValidation.addValidation(this, R.id.password, "^(?=.*[a-zA-Z\\d].*)[a-zA-Z\\d!@#$%&*]{7,}$", R.string.pwderror);


        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signUpIntent = new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(signUpIntent);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = memail.getEditText().getText().toString();
                String password = mpassword.getEditText().getText().toString();
                if(validate()) {
                    mProgressDailog.setMessage("Please wait while we log you in....");
                    mProgressDailog.show();
                    LoginUser(email, password);
                }
            }
        });
    }

    public void LoginUser(String email, String password)
    {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            mProgressDailog.dismiss();
                            String currentUserId = mAuth.getCurrentUser().getUid();
                            String deviceToken = FirebaseInstanceId.getInstance().getToken();

                            mUserDatabase.child(currentUserId).child("device_token").setValue(deviceToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toasty.success(LoginActivity.this,"Login Success", Toast.LENGTH_LONG, true).show();
                                    Intent mainIntent = new Intent(LoginActivity.this,MainActivity.class);
                                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(mainIntent);
                                    finish();
                                }
                            });

                        } else {
                            Toasty.error(LoginActivity.this, "Login Failed", Toast.LENGTH_LONG, true).show();                            mProgressDailog.hide();
                        }
                    }
                });
    }

    public boolean validate() {
        boolean valid = false;

        if(awesomeValidation.validate())
        {
            valid = true;
        }

        /*String email = memail.getEditText().getText().toString();
        String password = mpassword.getEditText().getText().toString();

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
        }
*/
        return valid;
    }
}
