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

public class TreatedCaseAdapter extends RecyclerView.Adapter<TreatedCaseAdapter.VH> {

    public interface Listener {
        void onEdit(TreatedCase t);
        void onDelete(TreatedCase t);
    }

    private final List<TreatedCase> items = new ArrayList<>();
    private final Listener listener;

    public TreatedCaseAdapter(Listener listener) {
        this.listener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setItems(List<TreatedCase> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_treated, parent, false);
        return new VH(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        TreatedCase t = items.get(position);

        h.tvDate.setText(t.getDate());
        h.tvDescription.setText(t.getDescription());
        h.tvProcedures.setText("الإجراءات: " + t.getProcedures());

        // صورة الحالة
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
                    // صورة تالفة أو كبيرة — نتجاهلها بهدوء
                }
            }
        }

        if (!hasValidPhoto) {
            h.ivPhoto.setVisibility(View.GONE);
            h.ivPhoto.setImageDrawable(null);
        }

        // ضبط margin حسب وجود الصورة
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams)
                h.tvDescription.getLayoutParams();
        lp.setMarginStart(hasValidPhoto ? dpToPx(h.itemView) : 0);
        h.tvDescription.setLayoutParams(lp);

        h.btnEdit.setOnClickListener(v -> { if (listener != null) listener.onEdit(t); });
        h.btnDelete.setOnClickListener(v -> { if (listener != null) listener.onDelete(t); });
    }

    private int dpToPx(View v) {
        return (int) (8 * v.getResources().getDisplayMetrics().density);
    }

    @Override
    public int getItemCount() { return items.size(); }

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