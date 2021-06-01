package com.poc.studytracker.subjects.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.poc.corea.models.subjects.Subject;
import com.poc.studytracker.R;
import com.poc.studytracker.common.adapter.OnItemClickListener;

import java.util.ArrayList;
import java.util.List;

public class SubjectsAdapter extends RecyclerView.Adapter<SubjectsAdapter.SubjectsViewHolder>{

    private List<Subject> mItems;
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_subject_list_item, null);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(lp);
        return new SubjectsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectsViewHolder holder, int position) {
        holder.subjectTitle.setText(mItems.get(position).subjectTitle);
        holder.btnGotoSession.setOnClickListener(v -> mListener.onButtonClickOnItem(GOTO_SESSION_BTN, position));
        holder.itemView.setOnClickListener(v -> mListener.onItemClick(position));
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void setmItems(List<Subject> mItems) {
        this.mItems = mItems;
        notifyDataSetChanged();
    }

    public List<Subject> getItems() {
        return this.mItems;
    }

    public Subject getItem(int pos) {
       return this.mItems.get(pos);
    }

    public static class SubjectsViewHolder extends RecyclerView.ViewHolder {

        TextView subjectTitle;
        ImageButton btnGotoSession;
        Button btnGotoSummary;

        public SubjectsViewHolder(@NonNull View itemView) {
            super(itemView);
            subjectTitle = itemView.findViewById(R.id.subjectTitle);
            btnGotoSession = itemView.findViewById(R.id.btnGotoSessions);
            btnGotoSummary = itemView.findViewById(R.id.btnGotoSummary);
        }
    }

}
