package com.example.betterbagwork;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private List<WorkoutHistory> historyList;
    private OnHistoryClickListener listener;

    public HistoryAdapter() {
        this.historyList = new ArrayList<>();
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        WorkoutHistory history = historyList.get(position);
        holder.bind(history);
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public void setHistory(List<WorkoutHistory> historyList) {
        this.historyList = historyList != null ? historyList : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setOnHistoryClickListener(OnHistoryClickListener listener) {
        this.listener = listener;
    }

    class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView txtWorkoutName, txtDate, txtRounds, txtDuration, txtStatus;
        MaterialButton btnDelete;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            txtWorkoutName = itemView.findViewById(R.id.txtWorkoutName);
            txtDate = itemView.findViewById(R.id.txtDate);
            txtRounds = itemView.findViewById(R.id.txtRounds);
            txtDuration = itemView.findViewById(R.id.txtDuration);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            btnDelete = itemView.findViewById(R.id.btnDeleteHistory);
        }

        public void bind(WorkoutHistory history) {
            txtWorkoutName.setText(history.getWorkoutName());
            txtDate.setText(history.getFormattedDate());
            txtRounds.setText(history.getRoundsCompleted() + "/" + history.getTotalRounds() + " Runden");
            txtDuration.setText(history.getFormattedDuration());

            // Status Badge
            txtStatus.setText(history.getStatusText());
            if (history.isCompleted()) {
                txtStatus.setBackgroundResource(R.drawable.status_badge_completed);
            } else {
                txtStatus.setBackgroundResource(R.drawable.status_badge_aborted);
            }

            // Delete Button
            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(history);
                }
            });
        }
    }

    public interface OnHistoryClickListener {
        void onDeleteClick(WorkoutHistory history);
    }
}