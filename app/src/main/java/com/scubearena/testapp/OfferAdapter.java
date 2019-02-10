package com.scubearena.testapp;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class OfferAdapter  extends RecyclerView.Adapter<OfferViewHolder> {

    List<Offer> list;

    public OfferAdapter(List<Offer> list) {
        this.list = list;
    }

    @Override
    public OfferViewHolder onCreateViewHolder(ViewGroup viewGroup, int itemType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.offer_card,viewGroup,false);
        return new OfferViewHolder(view);
    }
    @Override
    public void onBindViewHolder(OfferViewHolder myViewHolder, int position) {
        Offer myOffer = list.get(position);
        myViewHolder.bind(myOffer);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

}
