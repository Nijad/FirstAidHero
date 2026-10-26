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
 * Adapter لعرض خطوات الإسعاف مرقّمة.
 */
public class StepAdapter extends RecyclerView.Adapter<StepAdapter.VH> {

    private final List<ActionStep> items = new ArrayList<>();

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_step, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        ActionStep s = items.get(position);
        h.tvNumber.setText(String.valueOf(s.getStepNumber()));
        h.tvInstruction.setText(s.getInstruction());
    }

    @Override
    public int getItemCount() { return items.size(); }

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