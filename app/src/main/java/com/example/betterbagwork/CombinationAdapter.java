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

public class CombinationAdapter extends RecyclerView.Adapter<CombinationAdapter.CombinationViewHolder> {

    private List<Combination> combinations;
    private OnCombinationClickListener listener;

    public CombinationAdapter() {
        this.combinations = new ArrayList<>();
    }

    @NonNull
    @Override
    public CombinationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_combination, parent, false);
        return new CombinationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CombinationViewHolder holder, int position) {
        Combination combination = combinations.get(position);
        holder.bind(combination);
    }

    @Override
    public int getItemCount() {
        return combinations.size();
    }

    // Liste aktualisieren
    public void setCombinations(List<Combination> combinations) {
        this.combinations = combinations != null ? combinations : new ArrayList<>();
        notifyDataSetChanged();
    }

    // Listener setzen
    public void setOnCombinationClickListener(OnCombinationClickListener listener) {
        this.listener = listener;
    }

    // ViewHolder
    class CombinationViewHolder extends RecyclerView.ViewHolder {
        TextView txtCombinationName, txtCombinationMoves, txtMoveCount;
        Button btnDelete;

        public CombinationViewHolder(@NonNull View itemView) {
            super(itemView);
            txtCombinationName = itemView.findViewById(R.id.txtCombinationName);
            txtCombinationMoves = itemView.findViewById(R.id.txtCombinationMoves);
            txtMoveCount = itemView.findViewById(R.id.txtMoveCount);
            btnDelete = itemView.findViewById(R.id.btnDeleteCombination);
        }

        public void bind(Combination combination) {
            txtCombinationName.setText(combination.getName());
            txtCombinationMoves.setText(combination.getMovesAsString());
            txtMoveCount.setText(combination.getMoveCount() + " Schläge");

            // Click auf Item
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCombinationClick(combination);
                }
            });

            // Delete Button
            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(combination);
                }
            });
        }
    }

    // Interface für Klick-Events
    public interface OnCombinationClickListener {
        void onCombinationClick(Combination combination);
        void onDeleteClick(Combination combination);
    }
}