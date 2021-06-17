package in.agn.studytracker.common.adapter;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class BaseExpandableListAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    private HashMap<String, Boolean> parentItemCollapsedStateMap = new HashMap<>();
    private HashMap<String, List<ExpandableListItem>> parentChildMap = new HashMap<>();
    protected List<ExpandableListItem> mItems;
    protected List<ExpandableListItem> mItemsCopy;

    public static final int PARENT_TYPE = 34;
    public static final int CHILD_TYPE = 35;

    /*protected void toggle(String parentObjectId) {
        if(parentItemExpandedStateMap.get(parentObjectId) == null || parentItemExpandedStateMap.get(parentObjectId))
            parentItemExpandedStateMap.put(parentObjectId, false);
        else
            parentItemExpandedStateMap.put(parentObjectId, true);
        notifyDataSetChanged();
    }*/

    protected void collapse(String objectId) {
        mItems.removeAll(parentChildMap.get(objectId));
        notifyDataSetChanged();
        parentItemCollapsedStateMap.put(objectId, true);
    }

    protected void expand(int pos, String objectId) {
        mItems.addAll(pos + 1, parentChildMap.get(objectId));
        notifyDataSetChanged();
        parentItemCollapsedStateMap.put(objectId, false);
    }

    protected void toggle(int pos, String parentObjectId) {
        if(parentItemCollapsedStateMap.get(parentObjectId))
            expand(pos, parentObjectId);
        else
            collapse(parentObjectId);
    }

    /*protected void initializeParentItemCollapsedStateMap() {
        if(mItems != null && !mItems.isEmpty()) {
            for(ExpandableListItem listItem : mItems) {
                if(listItem.getType() == PARENT_TYPE)
                    parentItemCollapsedStateMap.put(listItem.getObjectId(), true);
            }
        }
    }*/

    protected void initializeItemExpandedStateMap() {
        parentChildMap.clear();
        parentItemCollapsedStateMap.clear();
        if(mItems != null && !mItems.isEmpty()) {
            mItemsCopy = new ArrayList<>();
            mItemsCopy.addAll(mItems);
            List<ExpandableListItem> childItemList = new ArrayList<>();
            for(ExpandableListItem listItem : mItemsCopy) {
                if(listItem.getType() == CHILD_TYPE) {
                    String parentId = listItem.getParentObjectId();
                    if(parentChildMap.get(parentId) != null) {
                        parentChildMap.get(parentId).add(listItem);
                    } else {
                        childItemList.add(listItem);
                        List<ExpandableListItem> childItemListCopy = new ArrayList<>();
                        childItemListCopy.addAll(childItemList);
                        parentChildMap.put(listItem.getParentObjectId(), childItemListCopy);
                    }
                    mItems.remove(listItem);
                } else if(listItem.getType() == PARENT_TYPE) {
                    childItemList.clear();
                    parentItemCollapsedStateMap.put(listItem.getObjectId(), true);
                }
            }
        }
    }

    protected boolean shouldDisplayChildItem(String parentObjectId) {
        if(parentObjectId == null)
            return false;
        return parentItemCollapsedStateMap.get(parentObjectId);
    }

    public List<ExpandableListItem> getmItemsCopy() {
        return mItemsCopy;
    }

    public static interface ExpandableListItem {
        int getType();
        String getObjectId();
        String getParentObjectId();
    }

    public class ExpandListListener implements View.OnClickListener {

        private String objectId;
        private int pos;

        public ExpandListListener(int pos, String objectId) {
            this.objectId = objectId;
            this.pos = pos;
        }

        @Override
        public void onClick(View v) {
            v.setSelected(!v.isSelected());
            toggle(pos, objectId);
        }
    }

}
