package com.scubearena.testapp;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class FullScreenImageActivity extends AppCompatActivity {

    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder build;
    int id = 1;

    ImageView imageView;
    private GestureDetector mGestureDetector;
    private ProgressDialog mProgressDailog;
    private Toolbar mToolbar;
    public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 123;

    private Bitmap bitmap;
    private ActionBar actionBar;

    private TextView mTitle;
    private TextView mLastseen;
    private CircleImageView mimageView;

    private DatabaseReference mRootRef;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreeimage);

        mToolbar = findViewById(R.id.full_screen_appBar);
        setSupportActionBar(mToolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(false);


        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = inflater.inflate(R.layout.chat_custom_bar,null);
        actionBar.setCustomView(actionBarView);

        // ---- Custom Action bar Items ----

        mTitle = findViewById(R.id.custom_bar_title);
        mLastseen = findViewById(R.id.custom_bar_seen);
        mimageView = findViewById(R.id.custom_bar_image);

        mGestureDetector = new GestureDetector(this, new GestureListener());
        mProgressDailog = new ProgressDialog(this,R.style.MyAlertDialogStyle);

        imageView = findViewById(R.id.fullscreenimageView);

        String mChatUserId = getIntent().getStringExtra("user_id");
        //String mChatUserName = getIntent().getStringExtra("user_name");
        String imageUrl = getIntent().getStringExtra("imageUrl");

        mRootRef = FirebaseDatabase.getInstance().getReference();

        mRootRef.child("Users").child(mChatUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                String online = dataSnapshot.child("online").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                String profImgStatus = dataSnapshot.child("profile_pic_hide").getValue().toString();
                mTitle.setText(name);
                if("Y".equalsIgnoreCase(profImgStatus))
                {
                    mimageView.setImageResource(R.drawable.default_contact);
                }
                else
                {
                    Picasso.get().load(image).placeholder(R.drawable.ic_launcher_background).into(mimageView);
                }
                if(online.equals("true"))
                {
                    mLastseen.setText("online");
                }
                else
                {
                    GetTimeAgo getTimeAgo = new GetTimeAgo();
                    long lastTime = Long.parseLong(online);
                    String lastSeenTime = getTimeAgo.getTimeAgo(lastTime,getApplicationContext());
                    mLastseen.setText(lastSeenTime);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mProgressDailog.setMessage("loading...");
        mProgressDailog.show();
        Picasso.get().load(imageUrl).into(imageView, new Callback() {
            @Override
            public void onSuccess() {
                bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();

                mProgressDailog.hide();
            }

            @Override
            public void onError(Exception e) {

            }
        });

    }
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {

        boolean eventConsumed=mGestureDetector.onTouchEvent(event);
        if (eventConsumed)
        {
            //Toast.makeText(this,GestureListener.currentGestureDetected,Toast.LENGTH_LONG).show();
            return true;
        }
        else
            return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.download_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.download_img)
        {
           //downloadImage(bitmap);
            mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            build = new NotificationCompat.Builder(FullScreenImageActivity.this);
            build.setContentTitle("Download")
                    .setContentText("Download in progress")
                    .setSmallIcon(R.drawable.ic_stat_notf_icon)
                    .setChannelId("download");


            new Thread(
                    new Runnable() {
                        @Override
                        public void run() {
                            int incr;
                            // Do the "lengthy" operation 20 times
                            for (incr = 0; incr <= 100; incr+=5)
                            {
                                // Sets the progress indicator to a max value, the current completion percentage and "determinate" state
                                build.setProgress(100, incr, false);
                                // Displays the progress bar for the first time.
                                mNotifyManager.notify(id, build.build());
                                // Sleeps the thread, simulating an operation
                                try {
                                    // Sleep for 1 second
                                    Thread.sleep(1*1000);
                                } catch (InterruptedException e) {
                                    Log.d("TAG", "sleep failure");
                                }
                            }
                            // When the loop is finished, updates the notification
                            build.setContentText("Download completed")
                                    // Removes the progress bar
                                    .setProgress(0,0,false);
                            mNotifyManager.notify(id, build.build());
                        }
                    }
                    // Starts the thread by calling the run() method in its Runnable
            ).start();

        }

        return super.onOptionsItemSelected(item);
    }

    private void downloadImage(Bitmap bitmap)
    {

        if (checkPermissionWRITE_EXTERNAL_STORAGE(this))
        {
            String savedImageURL = MediaStore.Images.Media.insertImage(
                    getContentResolver(),
                    bitmap,
                    getSaltString(),
                    "images"
            );

            // Parse the gallery image url to uri
            Uri savedImageURI = Uri.parse(savedImageURL);

            Toast.makeText(this,"Image saved to gallery. \n"+savedImageURI,Toast.LENGTH_LONG).show();
        }

    }

    public boolean checkPermissionWRITE_EXTERNAL_STORAGE(final Context context) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.WRITE_EXTERNAL_STORAGE))
                {
                    showDialog("External storage", context, Manifest.permission.WRITE_EXTERNAL_STORAGE);

                }
                else
                {
                    ActivityCompat.requestPermissions(
                            (Activity) context,
                            new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE },
                            MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                }
                return false;
            } else {
                return true;
            }

        } else {
            return true;
        }
    }

    public void showDialog(final String msg, final Context context, final String permission)
    {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle("Permission necessary");
        alertBuilder.setMessage(msg + " permission is necessary");
        alertBuilder.setPositiveButton(android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions((Activity) context,
                                new String[] { permission },
                                MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                    }
                });
        AlertDialog alert = alertBuilder.create();
        alert.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        switch (requestCode)
        {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    // do your stuff
                }
                else
                {
                    Toast.makeText(FullScreenImageActivity.this, "GET_ACCOUNTS Denied", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    protected String getSaltString() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 18) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;

    }

    private void createDirectoryAndSaveFile(Bitmap imageToSave, String fileName) {

        String location = Environment.getExternalStorageDirectory()+"/Gift";

        File direct = new File(location);

        if (!direct.exists()) {

            File wallpaperDirectory = new File(location);
            wallpaperDirectory.mkdirs();
        }

        File file = new File(new File(location), fileName+".jpeg");
        if (file.exists())
        {
            file.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(file);
            imageToSave.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            Toast.makeText(this,"Image saved to gallery. \n"+location+fileName,Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
