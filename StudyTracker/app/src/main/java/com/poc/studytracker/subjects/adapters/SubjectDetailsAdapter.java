package com.poc.studytracker.subjects.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.poc.studytracker.R;
import com.poc.studytracker.common.adapter.BaseExpandableListAdapter;
import com.poc.studytracker.subjects.models.SubjectPageModel;
import com.poc.studytracker.subjects.models.SubjectSectionModel;

import java.util.ArrayList;
import java.util.List;

public class SubjectDetailsAdapter extends BaseExpandableListAdapter<RecyclerView.ViewHolder> {

    public SubjectDetailsAdapter() {
        this.mItems = new ArrayList<>();
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
                sectionDetailsViewHolder.sectionProgressPercent.setText(subjectSectionModel.getSectionProgressPercent() + "% Completed");
                break;
            case CHILD_TYPE:
                SubjectPageModel subjectPageModel = (SubjectPageModel) listItem;
                SubjectPageDetailsViewHolder pageDetailsViewHolder = (SubjectPageDetailsViewHolder) holder;
                pageDetailsViewHolder.pageTitleTv.setText(subjectPageModel.getPageTitle());
                pageDetailsViewHolder.readStatus.setSelected(subjectPageModel.isRead());
                pageDetailsViewHolder.notesStatus.setSelected(subjectPageModel.isNotesTaken());
                pageDetailsViewHolder.memorizedStatus.setSelected(subjectPageModel.isMemorized());
                pageDetailsViewHolder.reviewStatus.setSelected(subjectPageModel.getReviewCount() > 0);
                pageDetailsViewHolder.practiceStatus.setSelected(subjectPageModel.getPracticeCount() > 0);
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

        Button expandButton;
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

        public SubjectPageDetailsViewHolder(@NonNull View itemView) {
            super(itemView);
            pageTitleTv = itemView.findViewById(R.id.pageTitle);
            readStatus = itemView.findViewById(R.id.imgReadStatus);
            memorizedStatus = itemView.findViewById(R.id.imgMemorizeStatus);
            notesStatus = itemView.findViewById(R.id.imgNotesStatus);
            practiceStatus = itemView.findViewById(R.id.imgPracticeStatus);
            reviewStatus = itemView.findViewById(R.id.imgReviewStatus);
        }
    }
}
