package com.scubearena.testapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;

public class OffersActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private TextView textView;
    private Button sendText;

    private RecyclerView recyclerView;

    private DatabaseReference mOffersDatabaseRef;
    private DatabaseReference userDatabaseRef;

    private FirebaseAuth mAuth;

    private String mCurrent_user_id;
    private String friendId;
    private String friendName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offers);

        friendId = getIntent().getStringExtra("user_id");
        //friendName = getIntent().getStringExtra("user_name");

        mToolbar = findViewById(R.id.offers_appBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        textView = findViewById(R.id.no_offers);
        sendText = findViewById(R.id.send_text);

        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        Date date = new Date();
        String currentDate = formatter.format(date);

        mAuth = FirebaseAuth.getInstance();

        mCurrent_user_id = mAuth.getCurrentUser().getUid();

        userDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child(friendId);
        userDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                getSupportActionBar().setTitle("Send gifts to "+name);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mOffersDatabaseRef = FirebaseDatabase.getInstance().getReference().child("EventInfo").child(currentDate).child(mCurrent_user_id).child(friendId).child("Offers");
        mOffersDatabaseRef.keepSynced(true);

        recyclerView = findViewById(R.id.recyclerView_offers);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //recyclerView.setLayoutManager(new GridLayoutManager(getActivity(),2));
        final LayoutAnimationController controller =
                AnimationUtils.loadLayoutAnimation(this, R.anim.layout_animation_fall_down);

        recyclerView.setLayoutAnimation(controller);
        recyclerView.scheduleLayoutAnimation();
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Offer, OfferViewHolder> offersRecyclerViewAdapter = new FirebaseRecyclerAdapter<Offer, OfferViewHolder>(

                Offer.class,
                R.layout.offer_card,
                OfferViewHolder.class,
                mOffersDatabaseRef

        ) {
            @Override
            protected void populateViewHolder(final OfferViewHolder offerViewHolder, Offer offers, int i) {

                final String list_Offer_id = getRef(i).getKey();

                mOffersDatabaseRef.child(list_Offer_id).addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        textView.setVisibility(View.INVISIBLE);

                        if (dataSnapshot.hasChild("offer")) {
                            final String offerName = dataSnapshot.child("offer").getValue().toString();
                            String offerImage = dataSnapshot.child("storeimage").getValue().toString();
                            String sendTo = dataSnapshot.child("sendTo").getValue().toString();
                            offerViewHolder.setName(offerName);
                            offerViewHolder.setImage(offerImage);
                            offerViewHolder.setSend("SEND",sendTo,offerName,offerImage);
                            offerViewHolder.setView(offerName,offerImage);
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };
        recyclerView.setAdapter(offersRecyclerViewAdapter);
        final LayoutAnimationController controller =
                AnimationUtils.loadLayoutAnimation(this, R.anim.layout_animation_fall_down);

        recyclerView.setLayoutAnimation(controller);
        recyclerView.getAdapter().notifyDataSetChanged();
        recyclerView.scheduleLayoutAnimation();
    }
}
