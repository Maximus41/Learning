package in.agn.studytracker.subjects.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.agn.corea.models.subjects.Subject;
import com.agn.studytracker.R;
import in.agn.studytracker.common.adapter.OnItemClickListener;
import in.agn.studytracker.subjects.models.SubjectUiModel;

import java.util.ArrayList;
import java.util.List;

public class SubjectsAdapter extends RecyclerView.Adapter<SubjectsAdapter.SubjectsViewHolder>{

    private List<SubjectUiModel> mItems;
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
        SubjectUiModel model = mItems.get(position);
        holder.subjectTitle.setText(model.getSubject().subjectTitle);
        holder.btnGotoSession.setOnClickListener(v -> mListener.onButtonClickOnItem(GOTO_SESSION_BTN, position));
        if(model.getNoOfSessions() > 0) {
            holder.sessionCount.setVisibility(View.VISIBLE);
            holder.sessionCount.setText(String.valueOf(model.getNoOfSessions()));
            if(model.isLastSessionActive())
                holder.sessionCount.setSelected(true);
            else
                holder.sessionCount.setSelected(false);
        } else {
            holder.sessionCount.setVisibility(View.GONE);
        }
        holder.itemView.setOnClickListener(v -> mListener.onItemClick(position));
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mListener.onItemLongClickListener(position, v);
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void setmItems(List<SubjectUiModel> mItems) {
        this.mItems = mItems;
        notifyDataSetChanged();
    }

    public List<SubjectUiModel> getItems() {
        return this.mItems;
    }

    public Subject getItem(int pos) {
       return this.mItems.get(pos).getSubject();
    }

    public static class SubjectsViewHolder extends RecyclerView.ViewHolder {

        TextView subjectTitle;
        ImageButton btnGotoSession;
        Button btnGotoSummary;
        TextView sessionCount;

        public SubjectsViewHolder(@NonNull View itemView) {
            super(itemView);
            subjectTitle = itemView.findViewById(R.id.subjectTitle);
            btnGotoSession = itemView.findViewById(R.id.btnGotoSessions);
            btnGotoSummary = itemView.findViewById(R.id.btnGotoSummary);
            sessionCount = itemView.findViewById(R.id.sessionCount);
        }
    }

}
