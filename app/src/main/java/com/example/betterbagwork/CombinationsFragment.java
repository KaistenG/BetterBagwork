package com.example.betterbagwork;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

public class CombinationsFragment extends Fragment {

    private RecyclerView recyclerViewCombinations;
    private Button btnCreateCombination;
    private CombinationAdapter combinationAdapter;
    private CombinationManager combinationManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_combinations, container, false);

        // Manager initialisieren
        combinationManager = new CombinationManager();

        // Views initialisieren
        recyclerViewCombinations = view.findViewById(R.id.recyclerViewCombinations);
        btnCreateCombination = view.findViewById(R.id.btnCreateCombination);

        // RecyclerView Setup
        setupRecyclerView();

        // Button: Neue Kombination erstellen
        btnCreateCombination.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CreateCombinationActivity.class);
            startActivity(intent);
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCombinations();
    }

    private void setupRecyclerView() {
        combinationAdapter = new CombinationAdapter();
        recyclerViewCombinations.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewCombinations.setAdapter(combinationAdapter);

        combinationAdapter.setOnCombinationClickListener(new CombinationAdapter.OnCombinationClickListener() {
            @Override
            public void onCombinationClick(Combination combination) {
                Toast.makeText(getContext(),
                        "Kombination: " + combination.getName(),
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDeleteClick(Combination combination) {
                showDeleteConfirmDialog(combination);
            }
        });
    }

    private void loadCombinations() {
        combinationManager.loadUserCombinations(getContext(), new CombinationManager.OnCombinationsLoadedListener() {
            @Override
            public void onSuccess(List<Combination> combinations) {
                combinationAdapter.setCombinations(combinations);
                if (combinations.isEmpty()) {
                    Toast.makeText(getContext(),
                            "Noch keine Kombinationen vorhanden",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String error) {
                // Fehler wird bereits im Manager angezeigt
            }
        });
    }

    private void showDeleteConfirmDialog(Combination combination) {
        new MaterialAlertDialogBuilder(getContext()) //ebenfalls wieder für das richtige Theme im Dialogfeld
                .setTitle("Kombination löschen?")
                .setMessage("Möchtest du '" + combination.getName() + "' wirklich löschen?")
                .setPositiveButton("Löschen", (dialog, which) -> deleteCombination(combination))
                .setNegativeButton("Abbrechen", null)
                .show();
    }

    private void deleteCombination(Combination combination) {
        combinationManager.deleteCombination(getContext(), combination.getId(),
                new CombinationManager.OnCombinationDeletedListener() {
                    @Override
                    public void onSuccess() {
                        loadCombinations();
                    }

                    @Override
                    public void onError(String error) {
                        // Fehler wird bereits im Manager angezeigt
                    }
                });
    }
}