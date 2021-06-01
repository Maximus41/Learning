package com.poc.studytracker.sessions.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.poc.studytracker.R;
import com.poc.studytracker.common.adapter.OnItemClickListener;
import com.poc.studytracker.sessions.models.AssessmentPageContentModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class AssessPageContentListAdapter extends RecyclerView.Adapter<AssessPageContentListAdapter.AssessPageViewHolder> {

    private List<AssessmentPageContentModel> mItems;
    public static final String EXPIRY_DATE_PATTERN = "dd MMM yyyy";
    public SimpleDateFormat simpleDateFormat;
    private OnItemClickListener mListener;

    public AssessPageContentListAdapter(OnItemClickListener listener) {
        this.mItems = new ArrayList<>();
        this.mListener = listener;
        simpleDateFormat = new SimpleDateFormat(EXPIRY_DATE_PATTERN);
    }

    @NonNull
    @Override
    public AssessPageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_assessment_page_content, null);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(lp);
        return new AssessPageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AssessPageViewHolder holder, int position) {
        AssessmentPageContentModel contentModel = mItems.get(position);
        holder.pageTitle.setText(contentModel.getPageTitle());
        holder.paraContent.setText(contentModel.getParaContent());
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void setmItems(List<AssessmentPageContentModel> mItems) {
        this.mItems = mItems;
        notifyDataSetChanged();
    }

    public List<AssessmentPageContentModel> getItems() {
        return this.mItems;
    }

    public AssessmentPageContentModel getItem(int pos) {
        return this.mItems.get(pos);
    }

    public static class AssessPageViewHolder extends RecyclerView.ViewHolder {

        TextView pageTitle;
        TextView paraContent;

        public AssessPageViewHolder(@NonNull View itemView) {
            super(itemView);
            pageTitle = itemView.findViewById(R.id.PageTitle);
            paraContent = itemView.findViewById(R.id.paraContent);
        }
    }

}
