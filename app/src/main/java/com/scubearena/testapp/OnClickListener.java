package com.scubearena.testapp;

import android.view.View;

public interface OnClickListener {

    void onItemClick(View view, int position);
    void onItemLongClick(View view, int position);

}
