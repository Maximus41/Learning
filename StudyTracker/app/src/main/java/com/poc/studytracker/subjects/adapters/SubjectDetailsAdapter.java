package com.poc.studytracker.subjects.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.poc.studytracker.R;
import com.poc.studytracker.common.adapter.BaseExpandableListAdapter;
import com.poc.studytracker.subjects.models.SubjectPageModel;
import com.poc.studytracker.subjects.models.SubjectSectionModel;

import java.util.List;

public class SubjectDetailsAdapter extends BaseExpandableListAdapter<RecyclerView.ViewHolder> {


    public SubjectDetailsAdapter(List<ExpandableListItem> items) {
        this.mItems = items;
        initializeItemExpandedStateMap();
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case PARENT_TYPE:
                return new SubjectSectionDetailsViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_subject_details_section_item, null));
            case CHILD_TYPE:
                return new SubjectPageDetailsViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_subject_details_page_item, null));
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
                sectionDetailsViewHolder.expandButton.setOnClickListener(new ExpandListListener(listItem.getObjectId()));
                sectionDetailsViewHolder.sectionTitleTv.setText(subjectSectionModel.getSectionTitle());
                break;
            case CHILD_TYPE:
                SubjectPageModel subjectPageModel = (SubjectPageModel) listItem;
                SubjectPageDetailsViewHolder pageDetailsViewHolder = (SubjectPageDetailsViewHolder) holder;
                if(shouldDisplayChildItem(listItem.getParentObjectId())) {
                    pageDetailsViewHolder.itemView.setVisibility(View.GONE);
                    return;
                } else {
                    pageDetailsViewHolder.itemView.setVisibility(View.VISIBLE);
                }
                    pageDetailsViewHolder.pageTitleTv.setText(subjectPageModel.getPage().getPageTitle());
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mItems.get(position).getType();
    }

    public static class SubjectSectionDetailsViewHolder extends RecyclerView.ViewHolder {

        Button expandButton;
        TextView sectionTitleTv;

        public SubjectSectionDetailsViewHolder(@NonNull View itemView) {
            super(itemView);
            expandButton = itemView.findViewById(R.id.btnExpandSection);
            sectionTitleTv = itemView.findViewById(R.id.sectionTitle);
        }
    }

    public static class SubjectPageDetailsViewHolder extends RecyclerView.ViewHolder {

        TextView pageTitleTv;

        public SubjectPageDetailsViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
