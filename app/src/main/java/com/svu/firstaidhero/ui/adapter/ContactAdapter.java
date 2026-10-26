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

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.VH> {

    public interface Listener {
        void onCall(EmergencyContact c);
        void onEdit(EmergencyContact c);
        void onDelete(EmergencyContact c);
    }

    private final List<EmergencyContact> items = new ArrayList<>();
    private final Listener listener;
    private final boolean showEditDelete;

    public ContactAdapter(Listener listener, boolean showEditDelete) {
        this.listener = listener;
        this.showEditDelete = showEditDelete;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setItems(List<EmergencyContact> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_contact, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        EmergencyContact c = items.get(position);

        h.tvName.setText(c.getName());
        h.tvPhone.setText(c.getPhone());
        h.tvIcon.setText(iconFor(c.getCategory()));

        // إظهار/إخفاء أزرار التعديل والحذف
        int visibility = showEditDelete ? View.VISIBLE : View.GONE;
        h.btnEdit.setVisibility(visibility);
        h.btnDelete.setVisibility(visibility);

        h.btnCall.setOnClickListener(v -> { if (listener != null) listener.onCall(c); });
        h.btnEdit.setOnClickListener(v -> { if (listener != null) listener.onEdit(c); });
        h.btnDelete.setOnClickListener(v -> { if (listener != null) listener.onDelete(c); });
    }

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

    @Override
    public int getItemCount() { return items.size(); }

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