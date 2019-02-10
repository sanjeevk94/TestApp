package com.scubearena.testapp;


import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.MyViewHolder> {

    private List<Friends> friendsList;
    private List<String> selectedIds = new ArrayList<>();

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name, status;
        CircleImageView image;
        RelativeLayout rootView;

        public MyViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.prof_name);
            image = view.findViewById(R.id.prof_img);
            status = view.findViewById(R.id.prof_status);
            rootView = view.findViewById(R.id.user_single_layout);
            }
        }

        public FriendsAdapter(List<Friends> friendsList) {
            this.friendsList = friendsList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.users_single_layout, parent, false);

            return new MyViewHolder(itemView);
        }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Friends friends = friendsList.get(position);
        holder.name.setText(friends.getName());
        Picasso.get().load(friends.getImage()).into(holder.image);
        holder.status.setText(friends.getStatus());

        String id = friendsList.get(position).getId();

        if (selectedIds.contains(id)){
            //if item is selected then,set foreground color of FrameLayout.
            holder.rootView.setBackgroundColor(Color.parseColor("#82c3b8"));
        }
        else {
            holder.rootView.setBackgroundColor(Color.parseColor("#ffffff"));
            //else remove selected item color.

        }
    }

    @Override
    public int getItemCount() {
        return friendsList.size();
    }

    public Friends getItem(int position)
    {
        return friendsList.get(position);
    }

    public void setSelectedIds(List<String> selectedIds)
    {
        this.selectedIds = selectedIds;
        notifyDataSetChanged();
    }

    }

