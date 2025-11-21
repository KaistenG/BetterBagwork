package com.example.betterbagwork;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder> {

    private List<Workout> workouts;
    private OnWorkoutClickListener listener;

    public WorkoutAdapter() {
        this.workouts = new ArrayList<>();
    }

    @NonNull
    @Override
    public WorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_workout, parent, false);
        return new WorkoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutViewHolder holder, int position) {
        Workout workout = workouts.get(position);
        holder.bind(workout);
    }

    @Override
    public int getItemCount() {
        return workouts.size();
    }

    public void setWorkouts(List<Workout> workouts) {
        this.workouts = workouts != null ? workouts : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setOnWorkoutClickListener(OnWorkoutClickListener listener) {
        this.listener = listener;
    }

    class WorkoutViewHolder extends RecyclerView.ViewHolder {
        TextView txtWorkoutName, txtRounds, txtRoundTime, txtTotalTime;
        Button btnStart, btnDelete;

        public WorkoutViewHolder(@NonNull View itemView) {
            super(itemView);
            txtWorkoutName = itemView.findViewById(R.id.txtWorkoutName);
            txtRounds = itemView.findViewById(R.id.txtRounds);
            txtRoundTime = itemView.findViewById(R.id.txtRoundTime);
            txtTotalTime = itemView.findViewById(R.id.txtTotalTime);
            btnStart = itemView.findViewById(R.id.btnStartWorkout);
            btnDelete = itemView.findViewById(R.id.btnDeleteWorkout);
        }

        public void bind(Workout workout) {
            txtWorkoutName.setText(workout.getName());
            txtRounds.setText(workout.getNumberOfRounds() + " Runden");
            txtRoundTime.setText("Rundenzeit: " + workout.getRoundTimeFormatted());
            txtTotalTime.setText("Gesamt: " + workout.getTotalDurationFormatted());

            // Start Button
            btnStart.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onWorkoutClick(workout);
                }
            });

            // Delete Button
            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(workout);
                }
            });
        }
    }

    public interface OnWorkoutClickListener {
        void onWorkoutClick(Workout workout);
        void onDeleteClick(Workout workout);
    }
}