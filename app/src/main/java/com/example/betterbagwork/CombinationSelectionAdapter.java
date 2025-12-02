package com.example.betterbagwork;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CombinationSelectionAdapter extends RecyclerView.Adapter<CombinationSelectionAdapter.SelectionViewHolder> {

    private List<Combination> combinations;
    private Set<String> selectedIds;

    public CombinationSelectionAdapter() {
        this.combinations = new ArrayList<>();
        this.selectedIds = new HashSet<>();
    }

    @NonNull
    @Override
    public SelectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_combination_selection, parent, false);
        return new SelectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SelectionViewHolder holder, int position) {
        Combination combination = combinations.get(position);
        holder.bind(combination, selectedIds.contains(combination.getId()));
    }

    @Override
    public int getItemCount() {
        return combinations.size();
    }

    public void setCombinations(List<Combination> combinations) {
        this.combinations = combinations != null ? combinations : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setSelectedCombinations(List<String> combinationIds) {
        if (combinationIds != null) {
            this.selectedIds = new HashSet<>(combinationIds);
            notifyDataSetChanged();
        }
    }

    public List<String> getSelectedCombinationIds() {
        return new ArrayList<>(selectedIds);
    }

    class SelectionViewHolder extends RecyclerView.ViewHolder {
        TextView txtCombinationName, txtCombinationMoves;
        CheckBox checkBox;

        public SelectionViewHolder(@NonNull View itemView) {
            super(itemView);
            txtCombinationName = itemView.findViewById(R.id.txtCombinationName);
            txtCombinationMoves = itemView.findViewById(R.id.txtCombinationMoves);
            checkBox = itemView.findViewById(R.id.checkBoxSelect);
        }

        public void bind(Combination combination, boolean isSelected) {
            txtCombinationName.setText(combination.getName());
            txtCombinationMoves.setText(combination.getMovesAsString());
            checkBox.setChecked(isSelected);

            // Click auf gesamtes Item
            itemView.setOnClickListener(v -> {
                boolean newState = !checkBox.isChecked();
                checkBox.setChecked(newState);
                updateSelection(combination.getId(), newState);
            });

            // Click auf Checkbox
            checkBox.setOnClickListener(v -> {
                updateSelection(combination.getId(), checkBox.isChecked());
            });
        }

        private void updateSelection(String id, boolean isSelected) {
            if (isSelected) {
                selectedIds.add(id);
            } else {
                selectedIds.remove(id);
            }
        }
    }
}