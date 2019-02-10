package com.scubearena.testapp;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;

public class PrivacyCustomAdapter extends BaseAdapter {
    private Context mContext;
    ArrayList<PrivacyItem> mylist=new ArrayList<>();

    public PrivacyCustomAdapter(ArrayList<PrivacyItem> itemArray,Context mContext) {
        super();
        this.mContext = mContext;
        mylist=itemArray;
    }

    @Override
    public int getCount() {
        return mylist.size();
    }

    @Override
    public String getItem(int position) {
        return mylist.get(position).toString();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void onItemSelected(int position) {
    }

    public class ViewHolder {
        public TextView nametext;
        public CheckBox tick;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent)
    {
        ViewHolder view = null;
        LayoutInflater inflator = ((Activity) mContext).getLayoutInflater();

        if (view == null)
        {
            view = new ViewHolder();
            convertView = inflator.inflate(R.layout.prv_custom_listview, null);
            view.nametext =  convertView.findViewById(R.id.adaptertextview);
            view.tick=convertView.findViewById(R.id.adaptercheckbox);
            view.tick.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView,
                                             boolean isChecked) {
                    int getPosition = (Integer) buttonView.getTag(); // Here
                    // we get  the position that we have set for the checkbox using setTag.
                    mylist.get(getPosition).setChecked(buttonView.isChecked()); // Set the value of checkbox to maintain its state.
                    String key=null,status=null;
                    if (isChecked) {
                        if(mylist.get(position).getTitle().equalsIgnoreCase("Hide your profile picture"))
                        {
                            key = "profile_pic_hide";
                            status = "Y";
                        }
                        else if(mylist.get(position).getTitle().equalsIgnoreCase("Hide your status"))
                        {

                            key = "profile_status_hide";
                            status = "Y";
                        }
                        else if(mylist.get(position).getTitle().equalsIgnoreCase("Hide your contact details"))
                        {

                            key = "profile_contact_details";
                            status = "Y";
                        }
                    }
                    else
                    {
                        if(mylist.get(position).getTitle().equalsIgnoreCase("Hide your profile picture"))
                        {
                            key = "profile_pic_hide";
                            status = "N";
                        }
                        else if(mylist.get(position).getTitle().equalsIgnoreCase("Hide your status"))
                        {

                            key = "profile_status_hide";
                            status = "N";
                        }
                        else if(mylist.get(position).getTitle().equalsIgnoreCase("Hide your contact details"))
                        {

                            key = "profile_contact_details";
                            status = "N";
                        }

                    }
                    setUserPrivacy(key,status);

                }
            });
            convertView.setTag(view);
        }
        else
            {
            view = (ViewHolder) convertView.getTag();
             }
        view.tick.setTag(position);
        view.nametext.setText("" + mylist.get(position).getTitle());
        view.tick.setChecked(mylist.get(position).isChecked());
        return convertView;
    }

    private void displayToast(String info)
    {
        Toasty.info(mContext,info,Toast.LENGTH_LONG).show();

    }
    private void setUserPrivacy(String key,String status)
    {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        Map updateHashMap = new HashMap<>();
        updateHashMap.put(key,status);
        userRef.updateChildren(updateHashMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if(task.isSuccessful())
                {
                }
            }
        });

          }

}
