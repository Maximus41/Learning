package com.poc.studytracker.sessions.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.poc.corea.models.session.Session;
import com.poc.studytracker.R;
import com.poc.studytracker.common.adapter.OnItemClickListener;

import java.util.ArrayList;
import java.util.List;

public class SessionsAdapter extends RecyclerView.Adapter<SessionsAdapter.SessionsViewHolder>{
    private List<Session> mItems;
    public static final int EDIT_SESSION_BTN = 10;
    public static final int START_SESSION_BTN = 11;
    private OnItemClickListener mListener;

    public SessionsAdapter(OnItemClickListener listener) {
        this.mItems = new ArrayList<>();
        this.mListener = listener;
    }

    @NonNull
    @Override
    public SessionsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SessionsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_session_list_item, null));
    }

    @Override
    public void onBindViewHolder(@NonNull SessionsViewHolder holder, int position) {
        holder.sessionTitle.setText(mItems.get(position).getSessionTitle());
        holder.btnEditSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onButtonClickOnItem(EDIT_SESSION_BTN, position);
            }
        });
        holder.btnStartSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onButtonClickOnItem(START_SESSION_BTN, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void setmItems(List<Session> mItems) {
        this.mItems = mItems;
        notifyDataSetChanged();
    }

    public List<Session> getItems() {
        return this.mItems;
    }

    public Session getItem(int pos) {
        return this.mItems.get(pos);
    }

    public static class SessionsViewHolder extends RecyclerView.ViewHolder {

        TextView sessionTitle;
        Button btnEditSession;
        Button btnStartSession;

        public SessionsViewHolder(@NonNull View itemView) {
            super(itemView);
            sessionTitle = itemView.findViewById(R.id.sessionTitle);
            btnEditSession = itemView.findViewById(R.id.btnEdit);
            btnStartSession = itemView.findViewById(R.id.btnStart);
        }
    }

}
