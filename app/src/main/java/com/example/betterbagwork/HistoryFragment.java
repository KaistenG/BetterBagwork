package com.example.betterbagwork;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

public class HistoryFragment extends Fragment {

    private RecyclerView recyclerViewHistory;
    private HistoryAdapter historyAdapter;
    private HistoryManager historyManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        // Manager initialisieren
        historyManager = new HistoryManager();

        // Views initialisieren
        recyclerViewHistory = view.findViewById(R.id.recyclerViewHistory);

        // RecyclerView Setup
        setupRecyclerView();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadHistory();
    }

    private void setupRecyclerView() {
        historyAdapter = new HistoryAdapter();
        recyclerViewHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewHistory.setAdapter(historyAdapter);

        historyAdapter.setOnHistoryClickListener(history -> showDeleteConfirmDialog(history));
    }

    private void loadHistory() {
        historyManager.loadUserHistory(getContext(), new HistoryManager.OnHistoryLoadedListener() {
            @Override
            public void onSuccess(List<WorkoutHistory> historyList) {
                historyAdapter.setHistory(historyList);
                if (historyList.isEmpty()) {
                    Toast.makeText(getContext(),
                            "Noch keine Workouts absolviert",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String error) {
                // Fehler wird bereits im Manager angezeigt
            }
        });
    }

    private void showDeleteConfirmDialog(WorkoutHistory history) {
        new MaterialAlertDialogBuilder(getContext())
                .setTitle("Eintrag löschen?")
                .setMessage("Möchtest du diesen History-Eintrag wirklich löschen?")
                .setPositiveButton("Löschen", (dialog, which) -> deleteHistory(history))
                .setNegativeButton("Abbrechen", null)
                .show();
    }

    private void deleteHistory(WorkoutHistory history) {
        historyManager.deleteHistory(getContext(), history.getId(),
                new HistoryManager.OnHistoryDeletedListener() {
                    @Override
                    public void onSuccess() {
                        loadHistory();
                    }

                    @Override
                    public void onError(String error) {
                        // Fehler wird bereits im Manager angezeigt
                    }
                });
    }
}