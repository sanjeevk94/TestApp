package com.scubearena.testapp;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public class EditEventAdapter extends RecyclerView.Adapter {

    private List<DataModel> dataSet;


    public EditEventAdapter(List<DataModel> data) {
        this.dataSet = data;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;


        return null;
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position)
    {

    }



    @Override
    public int getItemCount()
    {
        return dataSet.size();
    }
}
