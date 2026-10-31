package com.svu.firstaidhero.ui.adapter;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.svu.firstaidhero.R;
import com.svu.firstaidhero.model.FirstAidCase;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView Adapter for displaying the list of first aid cases.
 * Binds FirstAidCase data to the UI and handles user interactions.
 */
public class CaseAdapter extends RecyclerView.Adapter<CaseAdapter.VH> {

    /**
     * Listener interface for handling item click events.
     */
    public interface OnCaseClickListener {
        void onCaseClick(FirstAidCase c);
    }

    private final List<FirstAidCase> items = new ArrayList<>();
    private final OnCaseClickListener listener;

    /**
     * Constructs a new CaseAdapter.
     *
     * @param listener The callback to be invoked when a case is clicked.
     */
    public CaseAdapter(OnCaseClickListener listener) {
        this.listener = listener;
    }

    /**
     * Updates the list of items and refreshes the RecyclerView.
     *
     * @param newItems The new list of FirstAidCase objects to display.
     */
    @SuppressLint("NotifyDataSetChanged")
    public void setItems(List<FirstAidCase> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    /**
     * Creates the ViewHolder for individual list items by inflating the layout.
     */
    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_case, parent, false);
        return new VH(v);
    }

    /**
     * Binds the data of a specific FirstAidCase to the ViewHolder.
     * Updates the title, severity label, and changes the dot color based on the severity level.
     */
    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        FirstAidCase item = items.get(position);
        h.tvTitle.setText(item.getTitle());

        int colorRes;
        // Determine the color and string resource based on the case severity
        int labelRes = switch (item.getSeverity()) {
            case "critical" -> {
                colorRes = R.color.severity_critical;
                yield R.string.severity_critical;
            }
            case "medium" -> {
                colorRes = R.color.severity_medium;
                yield R.string.severity_medium;
            }
            default -> {
                colorRes = R.color.severity_minor;
                yield R.string.severity_minor;
            }
        };

        int color = ContextCompat.getColor(h.itemView.getContext(), colorRes);
        h.severityDot.setBackgroundTintList(ColorStateList.valueOf(color));
        h.tvSeverityLabel.setText(labelRes);

        // Handle item click
        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onCaseClick(item);
        });
    }

    /**
     * Returns the total number of items in the data set.
     */
    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * ViewHolder class to cache the views within each list item layout.
     */
    static class VH extends RecyclerView.ViewHolder {
        final TextView tvTitle;
        final TextView tvSeverityLabel;
        final View severityDot;

        VH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvSeverityLabel = itemView.findViewById(R.id.tvSeverityLabel);
            severityDot = itemView.findViewById(R.id.severityDot);
        }
    }
}