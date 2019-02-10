package com.scubearena.testapp;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import nl.dionsegijn.konfetti.KonfettiView;
import nl.dionsegijn.konfetti.models.Shape;
import nl.dionsegijn.konfetti.models.Size;

public class GiftsFragment extends Fragment {

    private View mMainView;

    private TextView textView;

    private RecyclerView recyclerView;

    private List<Offer> gifts = new ArrayList<>();

    private DatabaseReference mGiftsDatabaseRef;

    private FirebaseAuth mAuth;

    private String mCurrent_user_id;

    private KonfettiView konfettiView;




    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mMainView = inflater.inflate(R.layout.fragment_gift, container, false);
        textView = mMainView.findViewById(R.id.no_gifts);

        mAuth = FirebaseAuth.getInstance();

        mCurrent_user_id = mAuth.getCurrentUser().getUid();

        mGiftsDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Gifts").child(mCurrent_user_id);
        mGiftsDatabaseRef.keepSynced(true);

        recyclerView = mMainView.findViewById(R.id.recyclerView_gifts);

        konfettiView = mMainView.findViewById(R.id.konfettiView);
       /* konfettiView.build()
                .addColors(Color.YELLOW, Color.GREEN, Color.MAGENTA)
                .setDirection(0.0, 359.0)
                .setSpeed(1f, 5f)
                .setFadeOutEnabled(true)
                .setTimeToLive(2000L)
                .addShapes(Shape.RECT, Shape.CIRCLE)
                .addSizes(new Size(12, 5f))
                .setPosition(konfettiView.getX() + konfettiView.getWidth() / 2, konfettiView.getY() + konfettiView.getHeight() / 3)
                .burst(100);*/
               // .streamFor(300, 5000L)

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        recyclerView.setAdapter(new OfferAdapter(gifts));

        final LayoutAnimationController controller =
                AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_animation_fall_down);

        recyclerView.setLayoutAnimation(controller);
        recyclerView.scheduleLayoutAnimation();

        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<Offer, OfferViewHolder> offersRecyclerViewAdapter = new FirebaseRecyclerAdapter<Offer, OfferViewHolder>(

                Offer.class,
                R.layout.offer_card,
                OfferViewHolder.class,
                mGiftsDatabaseRef

        ) {
            @Override
            protected void populateViewHolder(final OfferViewHolder offerViewHolder, Offer offers, int i) {


                final String list_Offer_id = getRef(i).getKey();

                mGiftsDatabaseRef.child(list_Offer_id).addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        textView.setVisibility(View.INVISIBLE);

                        if(dataSnapshot.hasChild("gift")) {
                            final String giftName = dataSnapshot.child("gift").getValue().toString();
                            String giftImage = dataSnapshot.child("giftImage").getValue().toString();
                            offerViewHolder.setName(giftName);
                            offerViewHolder.setImage(giftImage);
                            offerViewHolder.setSend("CLAIM","",giftName,giftImage);
                            offerViewHolder.setView(giftName,giftImage);
                            konfettiView.build()
                                    .addColors(Color.YELLOW, Color.GREEN, Color.MAGENTA)
                                    .setDirection(0.0, 359.0)
                                    .setSpeed(1f, 5f)
                                    .setFadeOutEnabled(true)
                                    .setTimeToLive(2000L)
                                    .addShapes(Shape.RECT, Shape.CIRCLE)
                                    .addSizes(new Size(12, 5f))
                                    .setPosition(konfettiView.getX() + konfettiView.getWidth() / 2, konfettiView.getY() + konfettiView.getHeight() / 3)
                                    .burst(100);
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
                AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_animation_fall_down);

        recyclerView.setLayoutAnimation(controller);
        recyclerView.getAdapter().notifyDataSetChanged();
        recyclerView.scheduleLayoutAnimation();


    }
}
