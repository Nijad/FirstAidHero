package com.svu.firstaidhero.ui.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.svu.firstaidhero.R;
import com.svu.firstaidhero.model.EmergencyContact;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView Adapter for displaying the list of emergency contacts.
 * Handles binding contact data to the UI and manages interactions like calling, editing, and deleting.
 */
public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.VH> {

    /**
     * Listener interface to handle user actions on a specific contact.
     */
    public interface Listener {
        void onCall(EmergencyContact c);
        void onEdit(EmergencyContact c);
        void onDelete(EmergencyContact c);
    }

    private final List<EmergencyContact> items = new ArrayList<>();
    private final Listener listener;

    // Flag to determine if the Edit/Delete buttons should be visible (true for custom contacts, false for general)
    private final boolean showEditDelete;

    /**
     * Constructs a new ContactAdapter.
     *
     * @param listener       The callback for contact interactions.
     * @param showEditDelete Whether to display the edit and delete buttons.
     */
    public ContactAdapter(Listener listener, boolean showEditDelete) {
        this.listener = listener;
        this.showEditDelete = showEditDelete;
    }

    /**
     * Updates the list of contacts and refreshes the UI.
     *
     * @param newItems The new list of EmergencyContact objects.
     */
    @SuppressLint("NotifyDataSetChanged")
    public void setItems(List<EmergencyContact> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    /**
     * Creates the ViewHolder for individual contacts by inflating the layout.
     */
    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_contact, parent, false);
        return new VH(v);
    }

    /**
     * Binds the data of a specific EmergencyContact to the ViewHolder.
     * Sets text fields, assigns an icon based on the category, and configures click listeners.
     */
    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        EmergencyContact c = items.get(position);

        h.tvName.setText(c.getName());
        h.tvPhone.setText(c.getPhone());
        h.tvIcon.setText(iconFor(c.getCategory()));

        // Show/Hide edit and delete buttons based on the contact type (general vs custom)
        int visibility = showEditDelete ? View.VISIBLE : View.GONE;
        h.btnEdit.setVisibility(visibility);
        h.btnDelete.setVisibility(visibility);

        // Setup click listeners
        h.btnCall.setOnClickListener(v -> { if (listener != null) listener.onCall(c); });
        h.btnEdit.setOnClickListener(v -> { if (listener != null) listener.onEdit(c); });
        h.btnDelete.setOnClickListener(v -> { if (listener != null) listener.onDelete(c); });
    }

    /**
     * Helper method to assign an emoji icon based on the contact's category.
     *
     * @param category The category of the contact (e.g., ambulance, doctor).
     * @return A string containing the appropriate emoji.
     */
    private String iconFor(String category) {
        if (category == null) return "📞";
        return switch (category) {
            case "ambulance" -> "🚑";
            case "police" -> "🚓";
            case "fire" -> "🚒";
            case "red_crescent" -> "🏥";
            case "doctor" -> "👨‍⚕️";
            case "family" -> "👨‍👩‍👧";
            default -> "📞";
        };
    }

    /**
     * Returns the total number of items in the data set.
     */
    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * ViewHolder class to cache the views within each contact item layout.
     */
    static class VH extends RecyclerView.ViewHolder {
        final TextView tvIcon, tvName, tvPhone;
        final ImageView btnCall, btnEdit, btnDelete;

        VH(@NonNull View itemView) {
            super(itemView);
            tvIcon = itemView.findViewById(R.id.tvIcon);
            tvName = itemView.findViewById(R.id.tvName);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            btnCall = itemView.findViewById(R.id.btnCall);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}