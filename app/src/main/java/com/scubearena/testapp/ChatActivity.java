package com.scubearena.testapp;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;
import id.zelory.compressor.Compressor;

public class ChatActivity extends AppCompatActivity implements ActionMode.Callback,TextToSpeech.OnInitListener {

    private static final String TAG = ChatActivity.class.getSimpleName();

    private String mChatUser;
    private Toolbar mChatToolbar;
    private ActionMode actionMode;

    private DatabaseReference mRootRef;
    private DatabaseReference userDatabase;
    private String currentUserId;
    private ProgressDialog mProgressDialog;

    private String USE_CHAT_BG_FLAG="FALSE";


    private RelativeLayout chatLayout;

    private TextView mTitle;
    private TextView mLastseen;
    private CircleImageView mimageView;
    private FirebaseAuth mAuth;
    private String mCurrentUserId;

    private ImageButton mChatAddBtn;
    private ImageButton mChatAttach;
    private ImageButton mChatSendBtn;

    EmojiconEditText mChatMessageView;
    private boolean tapFlag;
    EmojIconActions emojIcon;
    View rootView;
    private ImageView mChatbg;
    private RecyclerView mMessagesList;
    private SwipeRefreshLayout mRefreshLayout;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private MessageAdapter mAdapter;


    private static final int TOTAL_ITEMS_TO_LOAD = 10;
    private int mcurrentPage = 1;

    private static final int GALLERY_PICK = 1;

    // Storage Firebase
    private StorageReference mImageStorage;

    //New Solution
    private int itemPos = 0;

    private String mLastKey = "";
    private String mPrevKey = "";

    GestureDetector gestureDetector;

    int deletePosition = 0;
    private Vibrator myVib;

    private TextToSpeech tts;

    private DatabaseReference chatBgDatabase;
    private String default_msg ="Hello buddy you there...";

    private final int REQ_CODE_SPEECH_INPUT = 100;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        gestureDetector = new GestureDetector(this, new GestureListener());

        chatLayout = findViewById(R.id.chatLayout);
        rootView = findViewById(R.id.chatLayout);

        mChatToolbar = findViewById(R.id.chat_app_bar);
        setSupportActionBar(mChatToolbar);

        final ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();


        mChatUser = getIntent().getStringExtra("user_id");
        //final String mChatUserName = getIntent().getStringExtra("user_name");


        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = inflater.inflate(R.layout.chat_custom_bar,null);

        actionBar.setCustomView(actionBarView);

        // ---- Custom Action bar Items ----

        mTitle = findViewById(R.id.custom_bar_title);
        mLastseen = findViewById(R.id.custom_bar_seen);
        mimageView = findViewById(R.id.custom_bar_image);

        mChatAddBtn = findViewById(R.id.chat_add_btn);
        mChatAttach = findViewById(R.id.chat_attach);
        mChatSendBtn = findViewById(R.id.chat_send_btn);
        myVib = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);



        mChatMessageView = findViewById(R.id.chat_message_view);
        emojIcon = new EmojIconActions(this, rootView, mChatMessageView, mChatAddBtn);
        emojIcon.ShowEmojIcon();
        emojIcon.setIconsIds(R.drawable.ic_action_keyboard, R.drawable.smiley);
        emojIcon.setKeyboardListener(new EmojIconActions.KeyboardListener() {
            @Override
            public void onKeyboardOpen() {
                Log.e(TAG, "Keyboard opened!");
            }

            @Override
            public void onKeyboardClose() {
                Log.e(TAG, "Keyboard closed");
            }
        });

        mChatbg = findViewById(R.id.chat_bg);

        tts = new TextToSpeech(this,this);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);

        userDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String msg = dataSnapshot.child("default_msg").getValue().toString();
                default_msg = msg;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        /*
        The below block of code for set Wallpaper for Individual Chat
         */
        chatBgDatabase = FirebaseDatabase.getInstance().getReference().child("ChatBackgrounds");


    chatBgDatabase.addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if(dataSnapshot.hasChild(currentUserId))
            {
                if(dataSnapshot.child(currentUserId).hasChild(mChatUser))
                {
                    String imageUrl = dataSnapshot.child(currentUserId).child(mChatUser).child("chat_bg_image").getValue().toString();
                    Picasso.get().load(imageUrl).placeholder(R.drawable.ic_chat_background).into(mChatbg);
                }
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    });

        mAdapter = new MessageAdapter(messagesList);

        mMessagesList = findViewById(R.id.messages_list);
        mRefreshLayout = findViewById(R.id.message_swipe_layout);
        mLinearLayout = new LinearLayoutManager(this);


        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearLayout);
        mMessagesList.setAdapter(mAdapter);



        mimageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profIntent = new Intent(ChatActivity.this,ProfileActivity.class);
                profIntent.putExtra("user_id",mChatUser);
                startActivity(profIntent);
            }
        });


        //----IMAGE STORAGE
        mImageStorage = FirebaseStorage.getInstance().getReference();

        mRootRef.child("Chat").child(mCurrentUserId).child(mChatUser).child("seen").setValue(true);
        loadMessages();


        mRootRef.child("Users").child(mChatUser).addValueEventListener(new ValueEventListener() {
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
                else {
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

        mRootRef.child("Chat").child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(!dataSnapshot.hasChild(mChatUser)){

                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen",false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/"+mCurrentUserId+"/"+mChatUser,chatAddMap);
                    chatUserMap.put("Chat/"+mChatUser+"/"+mCurrentUserId,chatAddMap);

                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError != null)
                            {
                                Log.d("CHAT_LOG",databaseError.getMessage());
                            }

                        }
                    });
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });




        mChatSendBtn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.message_sent);
                mp.start();
                sendMessage();
            }
        });

        mChatSendBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                myVib.vibrate(50);
                final MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.message_sent);
                tapFlag = true;
                mp.start();
                sendMessage();
                return false;
            }
        });


        mChatAttach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLERY_PICK);

            }
        });


        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
               // Toast.makeText(ChatActivity.this,"onRefresh()",Toast.LENGTH_LONG).show();

                mcurrentPage++;

                itemPos = 0;

                loadMoreMessages();
            }
        });


       // mChatMessageView.setOnTouchListener(touchListener);

        DatabaseReference friendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrentUserId).child(mChatUser);

        friendsDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists())
                {
                    LinearLayout linearLayout = findViewById(R.id.linearLayout);
                    linearLayout.setVisibility(View.INVISIBLE);
                    TextView info = findViewById(R.id.info);
                    info.setVisibility(View.VISIBLE);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mMessagesList.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), mMessagesList, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                if(messagesList.get(position).getType().equals("image"))
                {
                    Intent fullScreenIntent = new Intent(ChatActivity.this,FullScreenImageActivity.class);
                    fullScreenIntent.putExtra("user_id",mChatUser);
                    fullScreenIntent.putExtra("imageUrl",messagesList.get(position).getMessages());
                    startActivity(fullScreenIntent);
                }
            }

            @Override
            public void onLongClick(View view, int position) {
                myVib.vibrate(50);

                deletePosition = position;
                actionMode = ChatActivity.this.startActionMode(new ActionMode.Callback() {
             @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {

                 MenuInflater inflater = mode.getMenuInflater();
                 inflater.inflate(R.menu.chatmsg_menu, menu);
                 return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()){
                    case R.id.copy:
                        copyText();
                        actionMode.finish();
                        return true;
                    case R.id.frwd:
                        forwardText();
                        actionMode.finish();
                        return true;
                    case R.id.delete:
                        deleteText();
                        actionMode.finish();
                        return true;
                    case R.id.listen:
                        speakOut();
                        actionMode.finish();
                        return true;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                actionMode = null;

            }
        });

            }
        }));

    }

    /*@Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("Context Menu");
        menu.add(0, v.getId(), 0, "Upload");
        menu.add(0, v.getId(), 0, "Search");
        menu.add(0, v.getId(), 0, "Share");
        menu.add(0, v.getId(), 0, "Bookmark");
    }
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Toast.makeText(this, "Selected Item: " +item.getTitle(), Toast.LENGTH_SHORT).show();
        return true;
    }


    View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event)
        {
            boolean eventConsumed=gestureDetector.onTouchEvent(event);
                if (eventConsumed)
                {
                    if(GestureListener.currentGestureDetected.equalsIgnoreCase("onDoubleTapEvent"))
                    {
                        //Toasty.info(getApplicationContext(),GestureListener.currentGestureDetected,Toast.LENGTH_LONG).show();
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            myVib.vibrate(50);
                            final MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.notification);
                            tapFlag = true;
                            mp.start();
                            sendMessage();
                        }
                    }
                    else if (GestureListener.currentGestureDetected.equalsIgnoreCase("onSingleTapUp"))
                    {
                        //Toasty.info(getApplicationContext(),GestureListener.currentGestureDetected,Toast.LENGTH_LONG).show();
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(mChatMessageView, InputMethodManager.SHOW_IMPLICIT);
                    }
                    return true;
                }
                else
                {
                    return true;
                }
        }
    };*/




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(USE_CHAT_BG_FLAG.equalsIgnoreCase("TRUE"))
        {
            StorageReference mStorageRef;
            mStorageRef = FirebaseStorage.getInstance().getReference();

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
                    mProgressDialog = new ProgressDialog(ChatActivity.this,R.style.MyAlertDialogStyle);
                    mProgressDialog.setMessage("Please wait while we upload..");
                    mProgressDialog.setCanceledOnTouchOutside(false);
                    mProgressDialog.show();
                    Uri resultUri = result.getUri();
                    File thumbFilepath = new File(resultUri.getPath());
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
                    StorageReference filepath = mStorageRef.child("chat_bg_images").child(mCurrentUserId+mChatUser+".jpg");
                    final StorageReference thumbFile = mStorageRef.child("chat_bg_images").child("thumbnails").child(mCurrentUserId+mChatUser+".jpg");
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
                                        chatBgDatabase = FirebaseDatabase.getInstance().getReference().child("ChatBackgrounds").child(mCurrentUserId).child(mChatUser);
                                        if(thumbTask.isSuccessful())
                                        {
                                            Map updateHashMap = new HashMap<>();
                                            updateHashMap.put("chat_bg_image",downloadUrl);
                                            updateHashMap.put("chat_bg_thumb_image",thumbDownload);
                                            chatBgDatabase.updateChildren(updateHashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful())
                                                    {
                                                        mProgressDialog.dismiss();
                                                        Toasty.success(ChatActivity.this,"Wallpaper is Uploaded",Toast.LENGTH_LONG).show();
                                                        USE_CHAT_BG_FLAG="FALSE";
                                                    }
                                                }
                                            });
                                        }
                                        else
                                        {
                                            Toasty.error(ChatActivity.this,"Error in Uploading Wallpaper",Toast.LENGTH_LONG).show();
                                            mProgressDialog.dismiss();
                                        }
                                    }
                                });

                            }
                            else
                            {
                                Toasty.error(ChatActivity.this,"Error in Uploading",Toast.LENGTH_LONG).show();
                                mProgressDialog.dismiss();
                            }
                        }
                    });
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Exception error = result.getError();
                }
            }
        }

        else if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {

            mProgressDialog = new ProgressDialog(ChatActivity.this,R.style.MyAlertDialogStyle);
            mProgressDialog.setMessage("Please wait while we upload..");
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.show();

                Uri imageUri = data.getData();

                final String current_user_ref = "messages/" + mCurrentUserId + "/" + mChatUser;
                final String chat_user_ref = "messages/" + mChatUser + "/" + mCurrentUserId;

                DatabaseReference user_message_push = mRootRef.child("messages")
                        .child(mCurrentUserId).child(mChatUser).push();

                final String push_id = user_message_push.getKey();


                StorageReference filepath = mImageStorage.child("message_images").child(push_id + ".jpg");

                filepath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if (task.isSuccessful()) {

                            String download_url = task.getResult().getDownloadUrl().toString();


                            Map messageMap = new HashMap();
                            messageMap.put("messages", download_url);
                            messageMap.put("seen", false);
                            messageMap.put("type", "image");
                            messageMap.put("time", ServerValue.TIMESTAMP);
                            messageMap.put("from", mCurrentUserId);
                            messageMap.put("to", mChatUser);


                            HashMap<String, String> notificationData = new HashMap<>();
                            notificationData.put("from", mCurrentUserId);
                            notificationData.put("type", "request");

                            Map messageUserMap = new HashMap();
                            messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
                            messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);
                            messageUserMap.put("message_notifications/" + mChatUser + "/" + push_id, notificationData);

                            mChatMessageView.setText("");

                            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                    mProgressDialog.dismiss();
                                    if (databaseError != null) {

                                        Log.d("CHAT_LOG", databaseError.getMessage());

                                    }

                                }
                            });


                        }

                    }
                });

            }
        else if (resultCode == RESULT_OK && null != data && requestCode == REQ_CODE_SPEECH_INPUT)
        {

            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            mChatMessageView.setText(result.get(0));
        }

    }
    private void loadMoreMessages() {

        DatabaseReference messageRef = mRootRef.child("messages").child(mCurrentUserId).child(mChatUser);

        Query messageQuery = messageRef.orderByKey().endAt(mLastKey).limitToLast(10);
        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {


                Messages messages = dataSnapshot.getValue(Messages.class);
                String messageKey = dataSnapshot.getKey();

                if(!mPrevKey.equals(messageKey))
                {
                    messagesList.add(itemPos++,messages);

                }
                else
                {
                    mPrevKey = mLastKey;
                }

                if(itemPos == 1)
                {

                    mLastKey = messageKey;
                }

                Log.d("TOTALKEYS", "Last Key : " + mLastKey + " | Prev Key : " + mPrevKey + " | Message Key : " + messageKey);


                mAdapter.notifyDataSetChanged();
                mRefreshLayout.setRefreshing(false);
                mLinearLayout.scrollToPositionWithOffset(10,0);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    private void loadMessages(){

        final DatabaseReference messageRef = mRootRef.child("messages").child(mCurrentUserId).child(mChatUser);
        if(messageRef != null) {

            Query messageQuery = messageRef.limitToLast(mcurrentPage * TOTAL_ITEMS_TO_LOAD);

            messageQuery.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(final DataSnapshot dataSnapshot, String s) {

                    final Messages messages = dataSnapshot.getValue(Messages.class);
                    DatabaseReference chatRef = mRootRef.child("Chat").child(mChatUser).child(mCurrentUserId);
                    chatRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot indataSnapshot) {
                            if (indataSnapshot.hasChild("seen")) {
                                boolean status = (boolean) indataSnapshot.child("seen").getValue();
                                if (status) {
                                    DatabaseReference msgRef = mRootRef.child("messages").child(mCurrentUserId).child(mChatUser).child(dataSnapshot.getKey());
                                    msgRef.child("seen").setValue(true).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            messages.setSeen(true);
                                        }
                                    });

                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    itemPos++;

                    if (itemPos == 1) {
                        String messageKey = dataSnapshot.getKey();
                        mLastKey = messageKey;
                        mPrevKey = messageKey;
                    }

                    messagesList.add(messages);
                    mAdapter.notifyDataSetChanged();
                    mMessagesList.scrollToPosition(messagesList.size() - 1);


                    mRefreshLayout.setRefreshing(false);

                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

    }


    private void sendMessage()
    {

        String message;
        if(tapFlag == true)
        {
            message =default_msg;
            tapFlag = false;
        }
        else
            {
             message = mChatMessageView.getText().toString();
        }

        if(!TextUtils.isEmpty(message))
        {
            String currentUserRef = "messages/"+mCurrentUserId+"/"+mChatUser;
            String chatUserRef = "messages/"+mChatUser+"/"+mCurrentUserId;

            DatabaseReference userMessagePush = mRootRef.child("messages").child(mCurrentUserId).child(mChatUser).push();

            String pushId = userMessagePush.getKey();

        Map messageMap = new HashMap();
        messageMap.put("messages",message);
        messageMap.put("seen",false);
        messageMap.put("type","text");
        messageMap.put("time",ServerValue.TIMESTAMP);
        messageMap.put("from",mCurrentUserId);
        messageMap.put("to",mChatUser);

        /*DatabaseReference newNotificationRef = mRootRef.child("notifications").child(mChatUser).push();
        //String newNotificationId = newNotificationRef.getKey();*/

        HashMap<String,String> notificationData = new HashMap<>();
        notificationData.put("from",mCurrentUserId);
        notificationData.put("type","request");



        Map messageUserMap = new HashMap();
        messageUserMap.put(currentUserRef+"/"+pushId,messageMap);
        messageUserMap.put(chatUserRef+"/"+pushId,messageMap);
        messageUserMap.put("message_notifications/"+mChatUser+"/"+pushId,notificationData);
        mChatMessageView.setText("");

            mRootRef.child("Chat").child(mCurrentUserId).child(mChatUser).child("seen").setValue(true);
            mRootRef.child("Chat").child(mCurrentUserId).child(mChatUser).child("timestamp").setValue(ServerValue.TIMESTAMP);

            mRootRef.child("Chat").child(mChatUser).child(mCurrentUserId).child("seen").setValue(false);
            mRootRef.child("Chat").child(mChatUser).child(mCurrentUserId).child("timestamp").setValue(ServerValue.TIMESTAMP);

            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
               if(databaseError != null)
               {
                   Log.d("CHAT_LOG",databaseError.getMessage());
               }
            }
        });

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.change_chat_bg)
        {
            Intent galleryIntent = new Intent();
            galleryIntent.setType("image/*");
            galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

            USE_CHAT_BG_FLAG = "TRUE";
            startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLERY_PICK);
        }
        if(item.getItemId() == R.id.speak)
        {
            promptSpeechInput();
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.chatmsg_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.copy:
                copyText();
                actionMode.finish();
                return true;
            case R.id.frwd:
                forwardText();
                actionMode.finish();
                return true;
            case R.id.delete:
                deleteText();
                actionMode.finish();
                return true;
            case R.id.listen:
                speakOut();
                actionMode.finish();
                return true;
        }
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        actionMode = null;
    }


    private void copyText()
    {
        ClipboardManager clipboardManager = (ClipboardManager)
                getSystemService(Context.CLIPBOARD_SERVICE);

        CharSequence selectedTxt =  messagesList.get(deletePosition).getMessages();
        ClipData clipData = ClipData.newPlainText("Copied Text", selectedTxt);
        clipboardManager.setPrimaryClip(clipData);
        Toasty.info(ChatActivity.this,"Message copied",Toast.LENGTH_LONG).show();
    }

    private void deleteText()
    {
        DatabaseReference deleteMsgRef = FirebaseDatabase.getInstance().getReference().child("messages").child(currentUserId);
        Query deleteMsg = deleteMsgRef.child(mChatUser);

        deleteMsg.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot deleteMsgSnapshot : dataSnapshot.getChildren())
                {
                    if(deleteMsgSnapshot.child("messages").getValue()!= null) {
                        if (deleteMsgSnapshot.child("messages").getValue().toString().equalsIgnoreCase(messagesList.get(deletePosition).getMessages())
                                && deleteMsgSnapshot.child("time").getValue().toString().equalsIgnoreCase(String.valueOf(messagesList.get(deletePosition).getTime()))) {
                            deleteMsgSnapshot.getRef().removeValue();
                            messagesList.remove(deletePosition);
                            mAdapter = new MessageAdapter(messagesList);
                            mMessagesList.setHasFixedSize(true);
                            mMessagesList.setLayoutManager(mLinearLayout);
                            mMessagesList.setAdapter(mAdapter);
                            Toasty.info(ChatActivity.this, "Message deleted", Toast.LENGTH_LONG).show();

                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void forwardText()
    {
        Intent forwardIntent = new Intent(ChatActivity.this,ForwardActivity.class);
        String selectedTxt =  messagesList.get(deletePosition).getMessages();
        forwardIntent.putExtra("textToBeForwarded",selectedTxt);
        startActivity(forwardIntent);
    }

    private void pasteText() {
        ClipboardManager clipboardManager = (ClipboardManager)
                getSystemService(Context.CLIPBOARD_SERVICE);

        if(clipboardManager.hasPrimaryClip()) {
            ClipData.Item item = clipboardManager.getPrimaryClip().getItemAt(0);

            CharSequence ptext = item.getText();
            mChatMessageView.setText(ptext);
        }
    }


    @Override
    public void onInit(int status) {

        if (status == TextToSpeech.SUCCESS) {

            int result = tts.setLanguage(Locale.US);

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)
            {
                Log.e("TTS", "This Language is not supported");
            }
            else
                {
               // speakOut();
                }

        } else {
            Log.e("TTS", "Initilization Failed!");
        }

    }

    private void speakOut() {

        String text = messagesList.get(deletePosition).getMessages();

        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say Something");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toasty.info(getApplicationContext(),"Speech not supported", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        // Don't forget to shutdown tts!
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        if(mProgressDialog != null)
        {
            mProgressDialog.dismiss();
        }
        super.onDestroy();

    }
}
