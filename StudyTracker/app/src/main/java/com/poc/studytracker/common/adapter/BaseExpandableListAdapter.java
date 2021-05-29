package com.poc.studytracker.common.adapter;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.List;

public abstract class BaseExpandableListAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    private HashMap<String, Boolean> parentItemExpandedStateMap = new HashMap<>();
    protected List<ExpandableListItem> mItems;

    public static final int PARENT_TYPE = 34;
    public static final int CHILD_TYPE = 35;

    protected void toggle(String parentObjectId) {
        if(parentItemExpandedStateMap.get(parentObjectId) == null || parentItemExpandedStateMap.get(parentObjectId))
            parentItemExpandedStateMap.put(parentObjectId, false);
        else
            parentItemExpandedStateMap.put(parentObjectId, true);
        notifyDataSetChanged();
    }

    protected void initializeItemExpandedStateMap() {
        if(mItems != null && !mItems.isEmpty()) {
            for(ExpandableListItem listItem : mItems) {
                if(listItem.getType() == PARENT_TYPE)
                    parentItemExpandedStateMap.put(listItem.getObjectId(), false);
            }
        }
    }

    protected boolean shouldDisplayChildItem(String parentObjectId) {
        if(parentObjectId == null)
            return false;
        return parentItemExpandedStateMap.get(parentObjectId);
    }

    public static interface ExpandableListItem {
        int getType();
        String getObjectId();
        String getParentObjectId();
    }

    public class ExpandListListener implements View.OnClickListener {

        private String objectId;

        public ExpandListListener(String objectId) {
            this.objectId = objectId;
        }

        @Override
        public void onClick(View v) {
            toggle(objectId);
        }
    }

}
