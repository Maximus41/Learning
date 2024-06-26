package com.agn.studytracker.sessions.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.agn.studytracker.R;
import com.agn.studytracker.common.adapter.BaseExpandableListAdapter;
import com.agn.studytracker.common.adapter.OnItemClickListener;
import com.agn.studytracker.sessions.models.UpdateTopicPageModel;
import com.agn.studytracker.sessions.models.UpdateTopicSectionModel;

import java.util.ArrayList;
import java.util.List;

public class UpdateSessionAdapter extends BaseExpandableListAdapter<RecyclerView.ViewHolder>{
    private OnItemClickListener mListener;
    public static final int ACTION_READ = 45;
    public static final int ACTION_TAKE_NOTES = 46;
    public static final int ACTION_MEMORIZE = 47;
    public static final int ACTION_REVIEW = 48;
    public static final int ACTION_PRACTICE = 49;

    public UpdateSessionAdapter(OnItemClickListener listener) {
        this.mItems = new ArrayList<>();
        this.mListener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        switch (viewType) {
            case PARENT_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_session_update_section_status_item, null);
                view.setLayoutParams(lp);
                return new UpdateTopicSectionViewHolder(view);
            case CHILD_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_session_update_page_status_item, null);
                view.setLayoutParams(lp);
                return new UpdateTopicPageViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        BaseExpandableListAdapter.ExpandableListItem listItem = mItems.get(position);
        switch (getItemViewType(position)) {
            case PARENT_TYPE:
                UpdateTopicSectionModel topicSectionModel = (UpdateTopicSectionModel) listItem;
                UpdateTopicSectionViewHolder updateTopicSectionViewHolder = (UpdateTopicSectionViewHolder) holder;
                updateTopicSectionViewHolder.expandBtn.setOnClickListener(new ExpandListListener(position, listItem.getObjectId()));
                updateTopicSectionViewHolder.topicSectionTitle.setText(topicSectionModel.getSectionTitle());
//                updateTopicSectionViewHolder.topicSectionTitle.setSelected(true);
                //Todo:Calculate topic wise progress in a session
//                updateTopicSectionViewHolder.sectionProgressPercent.setText(topicSectionModel.getSessionTopicProgressPercent() + "% Progress");
                break;
            case CHILD_TYPE:
                UpdateTopicPageModel topicPageModel = (UpdateTopicPageModel) listItem;
                UpdateTopicPageViewHolder updateTopicPageViewHolder = (UpdateTopicPageViewHolder) holder;
                updateTopicPageViewHolder.topicPageTitle.setText(topicPageModel.getPageTitle());
//                updateTopicPageViewHolder.topicPageTitle.setSelected(true);
                updateTopicPageViewHolder.readBtn.setEnabled(!topicPageModel.isRead() && !topicPageModel.isActionsFreezed());
                updateTopicPageViewHolder.readBtn.setSelected(topicPageModel.isRead());
                updateTopicPageViewHolder.notesBtn.setEnabled(topicPageModel.isRead() && !topicPageModel.isNotesTaken() && !topicPageModel.isActionsFreezed());
                updateTopicPageViewHolder.notesBtn.setSelected(topicPageModel.isNotesTaken());
                updateTopicPageViewHolder.memorizedBtn.setEnabled(topicPageModel.isRead() && topicPageModel.isNotesTaken() && !topicPageModel.isMemorized() && !topicPageModel.isActionsFreezed());
                updateTopicPageViewHolder.memorizedBtn.setSelected(topicPageModel.isMemorized());
                updateTopicPageViewHolder.reviewBtn.setEnabled(topicPageModel.isRead() &&
                        topicPageModel.isNotesTaken() &&
                        topicPageModel.isMemorized());
                updateTopicPageViewHolder.practiceBtn.setEnabled(topicPageModel.isRead() &&
                        topicPageModel.isNotesTaken() &&
                        topicPageModel.isMemorized() &&
                        topicPageModel.getReviewCount() > 0);
                updateTopicPageViewHolder.reviewBtn.setSelected(topicPageModel.getReviewCount() > 0);
                updateTopicPageViewHolder.practiceBtn.setSelected(topicPageModel.getPracticeCount() > 0);

                updateTopicPageViewHolder.practiceCount.setVisibility(topicPageModel.getPracticeCount() > 0 ? View.VISIBLE : View.GONE);
                updateTopicPageViewHolder.reviewCount.setVisibility(topicPageModel.getReviewCount() > 0 ? View.VISIBLE : View.GONE);
                updateTopicPageViewHolder.reviewCount.setText(String.valueOf(topicPageModel.getReviewCount()));
                updateTopicPageViewHolder.practiceCount.setText(String.valueOf(topicPageModel.getPracticeCount()));

                updateTopicPageViewHolder.readBtn.setOnClickListener(v -> mListener.onButtonClickOnItem(ACTION_READ, position));
                updateTopicPageViewHolder.notesBtn.setOnClickListener(v -> mListener.onButtonClickOnItem(ACTION_TAKE_NOTES, position));
                updateTopicPageViewHolder.memorizedBtn.setOnClickListener(v -> mListener.onButtonClickOnItem(ACTION_MEMORIZE, position));
                updateTopicPageViewHolder.reviewBtn.setOnClickListener(v -> mListener.onButtonClickOnItem(ACTION_REVIEW, position));
                updateTopicPageViewHolder.practiceBtn.setOnClickListener(v -> mListener.onButtonClickOnItem(ACTION_PRACTICE, position));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mItems.get(position).getType();
    }

    public void setmItems(List<BaseExpandableListAdapter.ExpandableListItem> mItems) {
        this.mItems = mItems;
        initializeItemExpandedStateMap();
        notifyDataSetChanged();
    }

    public List<BaseExpandableListAdapter.ExpandableListItem> getItems() {
        return this.mItems;
    }

    public BaseExpandableListAdapter.ExpandableListItem getItem(int pos) {
        return this.mItems.get(pos);
    }

    public static class UpdateTopicSectionViewHolder extends RecyclerView.ViewHolder {

        TextView topicSectionTitle;
        ImageButton expandBtn;
        TextView sectionProgressPercent;

        public UpdateTopicSectionViewHolder(@NonNull View itemView) {
            super(itemView);
            topicSectionTitle = itemView.findViewById(R.id.sectionTitle);
            expandBtn = itemView.findViewById(R.id.btnExpandSection);
            sectionProgressPercent = itemView.findViewById(R.id.tvSectionProgressPercent);
        }
    }

    public static class UpdateTopicPageViewHolder extends RecyclerView.ViewHolder {

        TextView topicPageTitle;
        ImageButton readBtn;
        ImageButton notesBtn;
        ImageButton memorizedBtn;
        ImageButton reviewBtn;
        ImageButton practiceBtn;
        TextView reviewCount;
        TextView practiceCount;

        public UpdateTopicPageViewHolder(@NonNull View itemView) {
            super(itemView);
            topicPageTitle = itemView.findViewById(R.id.pageTitle);
            readBtn = itemView.findViewById(R.id.readStatusUpdateBtn);
            notesBtn = itemView.findViewById(R.id.notesTakenUpdateBtn);
            memorizedBtn = itemView.findViewById(R.id.memorizedNoteUpdateBtn);
            reviewBtn = itemView.findViewById(R.id.reviewButton);
            practiceBtn = itemView.findViewById(R.id.practiceButton);
            practiceCount = itemView.findViewById(R.id.practiceCount);
            reviewCount = itemView.findViewById(R.id.reviewCount);
        }
    }

}
