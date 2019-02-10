package com.scubearena.testapp;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

class SectionsPagerAdapter extends FragmentPagerAdapter{


    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        switch(position)
        {
            case 0:
                RequestsFragment requestsFragment = new RequestsFragment();
                return requestsFragment;
            case 1:
                ChatsFragment chatsFragment = new ChatsFragment();
                return chatsFragment;
            case 2:
                FriendsFragment friendsFragment = new FriendsFragment();
                return friendsFragment;
            case 3:
                EventsFragment EventFragment = new EventsFragment();
                return EventFragment;
            case 4:
                OffersFragment offerFragment = new OffersFragment();
                return offerFragment;
            case 5:
                GiftsFragment giftFragment = new GiftsFragment();
                return giftFragment;

            default :
                return null;
        }
    }

    @Override
    public int getCount() {
        return 6;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {

       /* switch(position)
        {
            case 0:
                return "REQUESTS";
            case 1:
                return "CHATS";
            case 2:
                return "FRIENDS";
            case 3:
                return "EVENTS";
            case 4:
                return "OFFERS";
            case 5:
                return "GIFTS";
            default:
                return null;
        }*/

       return null;

    }


}
