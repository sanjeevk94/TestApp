package com.scubearena.testapp;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ReceivedMessageHolder extends RecyclerView.ViewHolder {
    TextView messageText, timeText, nameText;
    ImageView profileImage;

    ReceivedMessageHolder(View itemView) {
        super(itemView);
        messageText = itemView.findViewById(R.id.text_message_body);
        timeText = itemView.findViewById(R.id.text_message_time);
        nameText = itemView.findViewById(R.id.text_message_name);
        profileImage = itemView.findViewById(R.id.image_message_profile);
    }

    void bind(Messages message) {
        messageText.setText(message.getMessages());

        final long time = message.getTime();
        final SimpleDateFormat sfd = new SimpleDateFormat("dd-MM HH:mm");
        sfd.format(new Date(time));

        // Format the stored timestamp into a readable String using method.
        timeText.setText(sfd.format(new Date(time)));
        nameText.setText(message.getFrom());

        // Insert the profile image from the URL into the ImageView.
        //Utils.displayRoundImageFromUrl(mContext, message.getSender().getProfileUrl(), profileImage);
        //Picasso.get().load().placeholder(R.drawable.ic_launcher_background).into(profileImage);
    }
}

