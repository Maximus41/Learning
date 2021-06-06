package com.poc.studytracker.common.adapter;

import android.view.View;

public interface OnItemClickListener {

    void onItemClick(int pos);
    void onItemLongClickListener(int pos, View view);
    void onButtonClickOnItem(int identifier, int pos);
}
