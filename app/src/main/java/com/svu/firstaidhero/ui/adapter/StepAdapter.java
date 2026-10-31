package com.svu.firstaidhero.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.svu.firstaidhero.R;
import com.svu.firstaidhero.model.ActionStep;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView Adapter for displaying numbered first aid action steps.
 * Binds ActionStep data to the UI elements.
 */
public class StepAdapter extends RecyclerView.Adapter<StepAdapter.VH> {

    private final List<ActionStep> items = new ArrayList<>();

    /**
     * Creates the ViewHolder for individual steps by inflating the layout.
     */
    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_step, parent, false);
        return new VH(v);
    }

    /**
     * Binds the data of a specific ActionStep to the ViewHolder.
     * Sets the step number and the instruction text.
     */
    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        ActionStep s = items.get(position);
        h.tvNumber.setText(String.valueOf(s.getStepNumber()));
        h.tvInstruction.setText(s.getInstruction());
    }

    /**
     * Returns the total number of steps in the data set.
     */
    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * ViewHolder class to cache the views within each step item layout.
     */
    static class VH extends RecyclerView.ViewHolder {
        final TextView tvNumber;
        final TextView tvInstruction;

        VH(@NonNull View itemView) {
            super(itemView);
            tvNumber = itemView.findViewById(R.id.tvStepNumber);
            tvInstruction = itemView.findViewById(R.id.tvInstruction);
        }
    }
}