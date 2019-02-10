package com.scubearena.testapp;

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

import java.text.SimpleDateFormat;
import java.util.Date;

public class OffersFragment extends Fragment {



    private View mMainView;

    private TextView textView,sendText;

    private RecyclerView recyclerView;

    private DatabaseReference mOffersDatabaseRef;

    private FirebaseAuth mAuth;

    private String mCurrent_user_id;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mMainView = inflater.inflate(R.layout.fragment_offers, container, false);
        textView = mMainView.findViewById(R.id.no_offers);
        sendText = mMainView.findViewById(R.id.send_text);
        textView.setVisibility(View.INVISIBLE);

        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        Date date = new Date();
        String currentDate = formatter.format(date);

        mAuth = FirebaseAuth.getInstance();

        mCurrent_user_id = mAuth.getCurrentUser().getUid();

        mOffersDatabaseRef = FirebaseDatabase.getInstance().getReference().child("EventInfo").child(currentDate).child(mCurrent_user_id).child("Offers");
        mOffersDatabaseRef.keepSynced(true);

        recyclerView = mMainView.findViewById(R.id.recyclerView_offers);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        //recyclerView.setLayoutManager(new GridLayoutManager(getActivity(),2));
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
                mOffersDatabaseRef

        ) {
            @Override
            protected void populateViewHolder(final OfferViewHolder offerViewHolder, Offer offers, int i) {

                final String list_Offer_id = getRef(i).getKey();
                System.out.print("**************** list_Offer_id :"+list_Offer_id+"*****************");

                    mOffersDatabaseRef.child(list_Offer_id).addValueEventListener(new ValueEventListener() {

                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            textView.setVisibility(View.INVISIBLE);

                            if (dataSnapshot.hasChild("offer")) {
                                        final String offerName = dataSnapshot.child("offer").getValue().toString();
                                        String offerImage = dataSnapshot.child("Storeimage").getValue().toString();
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
                AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_animation_fall_down);

        recyclerView.setLayoutAnimation(controller);
        recyclerView.getAdapter().notifyDataSetChanged();
        recyclerView.scheduleLayoutAnimation();
    }
}
