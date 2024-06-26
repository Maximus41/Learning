package com.agn.studytracker.subjects.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.agn.studytracker.R;
import com.agn.studytracker.common.adapter.BaseExpandableListAdapter;
import com.agn.studytracker.common.adapter.OnItemClickListener;
import com.agn.studytracker.subjects.models.SubjectPageModel;
import com.agn.studytracker.subjects.models.SubjectSectionModel;

import java.util.ArrayList;
import java.util.List;

public class SubjectDetailsAdapter extends BaseExpandableListAdapter<RecyclerView.ViewHolder> {

    private OnItemClickListener mListener;

    public SubjectDetailsAdapter(OnItemClickListener mListener) {
        this.mItems = new ArrayList<>();
        this.mListener = mListener;
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        switch (viewType) {
            case PARENT_TYPE:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_subject_details_section_item, null);
                view.setLayoutParams(lp);
                return new SubjectSectionDetailsViewHolder(view);
            case CHILD_TYPE:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_subject_details_page_item, null);
                view.setLayoutParams(lp);
                return new SubjectPageDetailsViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ExpandableListItem listItem = mItems.get(position);
        switch (listItem.getType()) {
            case PARENT_TYPE:
                SubjectSectionModel subjectSectionModel = (SubjectSectionModel) listItem;
                SubjectSectionDetailsViewHolder sectionDetailsViewHolder = (SubjectSectionDetailsViewHolder) holder;
                sectionDetailsViewHolder.expandButton.setOnClickListener(new ExpandListListener(position, listItem.getObjectId()));
                sectionDetailsViewHolder.sectionTitleTv.setText(subjectSectionModel.getSectionTitle());
//                sectionDetailsViewHolder.sectionTitleTv.setSelected(true);
                sectionDetailsViewHolder.sectionProgressPercent.setText(subjectSectionModel.getSectionProgressPercent() + "% Done");
                break;
            case CHILD_TYPE:
                SubjectPageModel subjectPageModel = (SubjectPageModel) listItem;
                SubjectPageDetailsViewHolder pageDetailsViewHolder = (SubjectPageDetailsViewHolder) holder;
                pageDetailsViewHolder.pageTitleTv.setText(subjectPageModel.getPageTitle());
//                pageDetailsViewHolder.pageTitleTv.setSelected(true);
                pageDetailsViewHolder.readStatus.setSelected(subjectPageModel.isRead());
                pageDetailsViewHolder.notesStatus.setSelected(subjectPageModel.isNotesTaken());
                pageDetailsViewHolder.memorizedStatus.setSelected(subjectPageModel.isMemorized());
                pageDetailsViewHolder.reviewStatus.setSelected(subjectPageModel.getReviewCount() > 0);
                pageDetailsViewHolder.practiceStatus.setSelected(subjectPageModel.getPracticeCount() > 0);
                pageDetailsViewHolder.practiceCount.setVisibility(subjectPageModel.getPracticeCount() > 0 ? View.VISIBLE : View.GONE);
                pageDetailsViewHolder.reviewCount.setVisibility(subjectPageModel.getReviewCount() > 0 ? View.VISIBLE : View.GONE);
                pageDetailsViewHolder.reviewCount.setText(String.valueOf(subjectPageModel.getReviewCount()));
                pageDetailsViewHolder.practiceCount.setText(String.valueOf(subjectPageModel.getPracticeCount()));
                pageDetailsViewHolder.itemView.setOnClickListener(v -> mListener.onItemClick(position));
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mItems.get(position).getType();
    }

    public void setItems(List<ExpandableListItem> items) {
        this.mItems = items;
        initializeItemExpandedStateMap();
    }

    public ExpandableListItem getItem(int pos) {
        return mItems.get(pos);
    }

    public static class SubjectSectionDetailsViewHolder extends RecyclerView.ViewHolder {

        ImageButton expandButton;
        TextView sectionTitleTv;
        TextView sectionProgressPercent;

        public SubjectSectionDetailsViewHolder(@NonNull View itemView) {
            super(itemView);
            expandButton = itemView.findViewById(R.id.btnExpandSection);
            sectionTitleTv = itemView.findViewById(R.id.sectionTitle);
            sectionProgressPercent = itemView.findViewById(R.id.tvSectionProgressPercent);
        }
    }

    public static class SubjectPageDetailsViewHolder extends RecyclerView.ViewHolder {

        TextView pageTitleTv;
        ImageView readStatus;
        ImageView notesStatus;
        ImageView memorizedStatus;
        ImageView reviewStatus;
        ImageView practiceStatus;
        TextView reviewCount;
        TextView practiceCount;

        public SubjectPageDetailsViewHolder(@NonNull View itemView) {
            super(itemView);
            pageTitleTv = itemView.findViewById(R.id.pageTitle);
            readStatus = itemView.findViewById(R.id.imgReadStatus);
            memorizedStatus = itemView.findViewById(R.id.imgMemorizeStatus);
            notesStatus = itemView.findViewById(R.id.imgNotesStatus);
            practiceStatus = itemView.findViewById(R.id.imgPracticeStatus);
            reviewStatus = itemView.findViewById(R.id.imgReviewStatus);
            practiceCount = itemView.findViewById(R.id.practiceCount);
            reviewCount = itemView.findViewById(R.id.reviewCount);
        }
    }
}
