package com.scubearena.testapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import es.dmoral.toasty.Toasty;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {

    private DatabaseReference mUserDatabase;
    private FirebaseUser mCurrentUser;
   // private CircleImageView circleImageView;
    private ImageView circleImageView;
    private FloatingActionButton fab1;
    private TextView mdisplayName;
    private TextView mstatus,listViewHeader;
    private static final int GALLERY_PICK=1;
    private static final int MAX_LENGTH=10;
    private StorageReference mStorageRef;
    private ProgressDialog mProgressDialog;
    private Toolbar toolbar;
    private String profileImage;

    private ListView listview;
    int[] listviewImage = new int[]{R.drawable.ic_email_black_24dp,R.drawable.ic_phone_android_black_24dp};
    ArrayAdapter<String> adapter;
    String[] listContents = new String[2];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        toolbar = findViewById(R.id.profile_appBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("My Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        circleImageView = findViewById(R.id.profile_image);
        fab1 = findViewById(R.id.chg_img);
        mdisplayName = findViewById(R.id.set_dis_name);
        mstatus = findViewById(R.id.set_status);
        listViewHeader = findViewById(R.id.lv_head);
        listview = findViewById(R.id.prf_list);

        mStorageRef = FirebaseStorage.getInstance().getReference();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        final String uid = mCurrentUser.getUid();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        mUserDatabase.keepSynced(true);

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("name").getValue().toString();
                String email = dataSnapshot.child("email").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();
                profileImage = image;
                String thumbImage = dataSnapshot.child("thumb_image").getValue().toString();
                mdisplayName.setText(name);
                mstatus.setText(status);
                listContents[0]=email;
                listContents[1]="+91-XXXXXXXXXX";
                List<HashMap<String, String>> aList = new ArrayList<>();

                for (int i = 0; i < 2; i++) {
                    HashMap<String, String> hm = new HashMap<>();
                    hm.put("listview_content", listContents[i]);
                    hm.put("listview_image", Integer.toString(listviewImage[i]));
                    aList.add(hm);
                }

                String[] from = {"listview_image", "listview_content"};
                int[] to = {R.id.listview_image, R.id.listview_item_title};

                SimpleAdapter simpleAdapter = new SimpleAdapter(getBaseContext(), aList, R.layout.acct_custome_listview, from, to);
                listview.setAdapter(simpleAdapter);
                if(!image.equals("default"))
                {
                    //Picasso.get().load(image).placeholder(R.drawable.ic_launcher_foreground).into(circleImageView);

                    Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.default_contact).into(circleImageView, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get().load(image).placeholder(R.drawable.default_contact).into(circleImageView);
                        }
                    });
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent fullScreenIntent = new Intent(SettingsActivity.this,FullScreenImageActivity.class);
                fullScreenIntent.putExtra("user_id",uid);
                fullScreenIntent.putExtra("imageUrl",profileImage);
                startActivity(fullScreenIntent);
            }
        });
        mstatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String statusValue = mstatus.getText().toString();
                Intent statusIntent = new Intent(SettingsActivity.this,StatusActivity.class);
                statusIntent.putExtra("statusValue",statusValue);
                startActivity(statusIntent);
            }
        });

        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

              /*  CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(SettingsActivity.this);*/

               Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent,"SELECT IMAGE"),GALLERY_PICK);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_PICK && resultCode == RESULT_OK)
        {
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setAspectRatio(1,1)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                mProgressDialog = new ProgressDialog(SettingsActivity.this,R.style.MyAlertDialogStyle);
                mProgressDialog.setMessage("Please wait while we upload..");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();
                Uri resultUri = result.getUri();
                File thumbFilepath = new File(resultUri.getPath());
                String uId = mCurrentUser.getUid();
                Bitmap thumbBitmap = null;
                try {
                   thumbBitmap = new Compressor(this)
                           .setMaxHeight(200)
                           .setMaxWidth(200)
                           .setQuality(75)
                           .compressToBitmap(thumbFilepath);
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumbBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                final byte[] thumbByte = baos.toByteArray();
                StorageReference filepath = mStorageRef.child("profile_images").child(uId+".jpg");
                final StorageReference thumbFile = mStorageRef.child("profile_images").child("thumbnails").child(uId+".jpg");
                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful())
                        {
                            final String downloadUrl = task.getResult().getDownloadUrl().toString();
                            UploadTask uploadTask = thumbFile.putBytes(thumbByte);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumbTask) {
                                    String thumbDownload = thumbTask.getResult().getDownloadUrl().toString();
                                    if(thumbTask.isSuccessful())
                                    {
                                        Map updateHashMap = new HashMap<>();
                                        updateHashMap.put("image",downloadUrl);
                                        updateHashMap.put("thumb_image",thumbDownload);
                                        mUserDatabase.updateChildren(updateHashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful())
                                                {
                                                    mProgressDialog.dismiss();
                                                    Toasty.success(SettingsActivity.this,"Profile picture is Uploaded",Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });
                                    }
                                    else
                                    {
                                        Toasty.error(SettingsActivity.this,"Error in Uploading thumbnail",Toast.LENGTH_LONG).show();
                                        mProgressDialog.dismiss();
                                    }
                                }
                            });

                        }
                        else
                        {
                            Toasty.error(SettingsActivity.this,"Error in Uploading",Toast.LENGTH_LONG).show();
                            mProgressDialog.dismiss();
                        }
                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
    public static String random() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(MAX_LENGTH);
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }
}
