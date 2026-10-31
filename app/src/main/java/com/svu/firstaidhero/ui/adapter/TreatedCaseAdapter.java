package com.svu.firstaidhero.ui.adapter;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.svu.firstaidhero.R;
import com.svu.firstaidhero.model.TreatedCase;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView Adapter for displaying the list of treated cases.
 * Handles loading attached photos, displaying case details,
 * and providing edit/delete actions.
 */
public class TreatedCaseAdapter extends RecyclerView.Adapter<TreatedCaseAdapter.VH> {

    /**
     * Listener interface for handling user actions on a treated case.
     */
    public interface Listener {
        void onEdit(TreatedCase t);
        void onDelete(TreatedCase t);
    }

    private final List<TreatedCase> items = new ArrayList<>();
    private final Listener listener;

    /**
     * Constructs a new TreatedCaseAdapter.
     *
     * @param listener The callback for handling edit and delete actions.
     */
    public TreatedCaseAdapter(Listener listener) {
        this.listener = listener;
    }

    /**
     * Updates the list of treated cases and refreshes the UI.
     *
     * @param newItems The new list of TreatedCase objects.
     */
    @SuppressLint("NotifyDataSetChanged")
    public void setItems(List<TreatedCase> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    /**
     * Creates the ViewHolder for individual treated cases by inflating the layout.
     */
    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_treated, parent, false);
        return new VH(v);
    }

    /**
     * Binds the data of a specific TreatedCase to the ViewHolder.
     * Sets text fields, loads the photo if it exists, and adjusts layout margins dynamically.
     */
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        TreatedCase t = items.get(position);

        h.tvDate.setText(t.getDate());
        h.tvDescription.setText(t.getDescription());
        h.tvProcedures.setText("الإجراءات: " + t.getProcedures());

        // Load the case photo
        String path = t.getPhotoPath();
        boolean hasValidPhoto = false;
        if (path != null && !path.isEmpty()) {
            File imgFile = new File(path);
            if (imgFile.exists() && imgFile.length() > 0) {
                try {
                    Bitmap bmp = BitmapFactory.decodeFile(path);
                    if (bmp != null) {
                        h.ivPhoto.setVisibility(View.VISIBLE);
                        h.ivPhoto.setImageBitmap(bmp);
                        hasValidPhoto = true;
                    }
                } catch (OutOfMemoryError | Exception e) {
                    // Corrupted or too large image — silently ignore to prevent a crash (OOM)
                }
            }
        }

        // Hide the image view if no valid photo exists
        if (!hasValidPhoto) {
            h.ivPhoto.setVisibility(View.GONE);
            h.ivPhoto.setImageDrawable(null);
        }

        // Dynamically adjust the start margin of the description text based on photo visibility
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams)
                h.tvDescription.getLayoutParams();
        lp.setMarginStart(hasValidPhoto ? dpToPx(h.itemView) : 0);
        h.tvDescription.setLayoutParams(lp);

        // Setup click listeners for edit and delete
        h.btnEdit.setOnClickListener(v -> { if (listener != null) listener.onEdit(t); });
        h.btnDelete.setOnClickListener(v -> { if (listener != null) listener.onDelete(t); });
    }

    /**
     * Helper method to convert 8dp to pixels based on the device's screen density.
     * Used for setting the dynamic margin.
     *
     * @param v A view used to access the context's display metrics.
     * @return The calculated pixel value.
     */
    private int dpToPx(View v) {
        return (int) (8 * v.getResources().getDisplayMetrics().density);
    }

    /**
     * Returns the total number of treated cases in the data set.
     */
    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * ViewHolder class to cache the views within each treated case layout.
     */
    static class VH extends RecyclerView.ViewHolder {
        final TextView tvDate, tvDescription, tvProcedures;
        final ImageView ivPhoto, btnEdit, btnDelete;

        VH(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvProcedures = itemView.findViewById(R.id.tvProcedures);
            ivPhoto = itemView.findViewById(R.id.ivPhoto);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}