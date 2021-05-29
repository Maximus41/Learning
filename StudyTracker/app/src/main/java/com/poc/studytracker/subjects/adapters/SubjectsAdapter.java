package com.poc.studytracker.subjects.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.poc.studytracker.R;
import com.poc.studytracker.common.adapter.OnItemClickListener;

import java.util.ArrayList;
import java.util.List;

public class SubjectsAdapter extends RecyclerView.Adapter<SubjectsAdapter.SubjectsViewHolder>{

    private List<String> mItems;
    public static final int GOTO_SESSION_BTN = 7;
    public static final int GOTO_SUMMARY_BTN = 9;
    private OnItemClickListener mListener;

    public SubjectsAdapter(OnItemClickListener listener) {
        this.mItems = new ArrayList<>();
        this.mListener = listener;
    }

    @NonNull
    @Override
    public SubjectsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SubjectsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_subject_list_item, null));
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectsViewHolder holder, int position) {
        holder.subjectTitle.setText(mItems.get(position));
        holder.btnGotoSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onButtonClickOnItem(GOTO_SESSION_BTN, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void setmItems(List<String> mItems) {
        this.mItems = mItems;
        notifyDataSetChanged();
    }

    public List<String> getItems() {
        return this.mItems;
    }

    public String getItem(int pos) {
       return this.mItems.get(pos);
    }

    public static class SubjectsViewHolder extends RecyclerView.ViewHolder {

        TextView subjectTitle;
        Button btnGotoSession;
        Button btnGotoSummary;

        public SubjectsViewHolder(@NonNull View itemView) {
            super(itemView);
            subjectTitle = itemView.findViewById(R.id.subjectTitle);
            btnGotoSession = itemView.findViewById(R.id.btnGotoSessions);
            btnGotoSummary = itemView.findViewById(R.id.btnGotoSummary);
        }
    }

}
