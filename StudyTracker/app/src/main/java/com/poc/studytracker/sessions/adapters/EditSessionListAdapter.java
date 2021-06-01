package com.poc.studytracker.sessions.adapters;

import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.poc.studytracker.R;
import com.poc.studytracker.common.adapter.BaseExpandableListAdapter;
import com.poc.studytracker.common.adapter.OnItemClickListener;
import com.poc.studytracker.sessions.models.TopicPageModel;
import com.poc.studytracker.sessions.models.TopicSectionModel;

import java.util.ArrayList;
import java.util.List;

public class EditSessionListAdapter extends BaseExpandableListAdapter<RecyclerView.ViewHolder> {
    private OnItemClickListener mListener;

    public EditSessionListAdapter(OnItemClickListener listener) {
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
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_session_section_item, null);
                view.setLayoutParams(lp);
                return new TopicSectionViewHolder(view);
            case CHILD_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_session_page_item, null);
                view.setLayoutParams(lp);
                return new TopicPageViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ExpandableListItem listItem = mItems.get(position);
        switch (getItemViewType(position)) {
            case PARENT_TYPE:
                TopicSectionModel topicSectionModel = (TopicSectionModel) listItem;
                TopicSectionViewHolder topicSectionViewHolder = (TopicSectionViewHolder) holder;
                topicSectionViewHolder.expandBtn.setOnClickListener(new ExpandListListener(position, listItem.getObjectId()));
                topicSectionViewHolder.topicSectionTitle.setText(topicSectionModel.getSectionTitle());
                break;
            case CHILD_TYPE:
                TopicPageModel topicPageModel = (TopicPageModel) listItem;
                TopicPageViewHolder topicPageViewHolder = (TopicPageViewHolder) holder;
                topicPageViewHolder.topicPageTitle.setText(topicPageModel.getPageTitle());
                topicPageViewHolder.itemView.setOnClickListener(v -> mListener.onItemClick(position));
                if(!TextUtils.isEmpty(topicPageModel.getParaformattedContent())) {
                    topicPageViewHolder.topicParaContent.setVisibility(View.VISIBLE);
                    topicPageViewHolder.topicParaContent.setText(Html.fromHtml(topicPageModel.getParaformattedContent()));
                } else {
                    topicPageViewHolder.topicParaContent.setVisibility(View.GONE);
                }
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

    public void setmItems(List<ExpandableListItem> mItems) {
        this.mItems = mItems;
        initializeItemExpandedStateMap();
        notifyDataSetChanged();
    }

    public List<ExpandableListItem> getItems() {
        return this.mItems;
    }

    public ExpandableListItem getItem(int pos) {
        return this.mItems.get(pos);
    }

    public static class TopicSectionViewHolder extends RecyclerView.ViewHolder {

        TextView topicSectionTitle;
        ImageButton expandBtn;

        public TopicSectionViewHolder(@NonNull View itemView) {
            super(itemView);
            topicSectionTitle = itemView.findViewById(R.id.sectionTitle);
            expandBtn = itemView.findViewById(R.id.btnExpandSection);
        }
    }

    public static class TopicPageViewHolder extends RecyclerView.ViewHolder {

        TextView topicPageTitle;
        TextView topicParaContent;

        public TopicPageViewHolder(@NonNull View itemView) {
            super(itemView);
            topicPageTitle = itemView.findViewById(R.id.pageTitle);
            topicParaContent = itemView.findViewById(R.id.paraContent);
        }
    }

}
