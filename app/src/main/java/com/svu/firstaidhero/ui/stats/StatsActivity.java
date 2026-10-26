package com.svu.firstaidhero.ui.stats;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.svu.firstaidhero.R;
import com.svu.firstaidhero.data.FirstAidRepository;

import java.util.Calendar;
import java.util.Map;

public class StatsActivity extends AppCompatActivity {

    private FirstAidRepository repo;

    private TextView tvTotalCases, tvTotalTreated, tvTotalContacts;
    private LinearLayout containerSeverity, containerTreatedBySeverity;
    private TextView tvPickedDate, tvDateCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        repo = FirstAidRepository.getInstance(this);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        tvTotalCases = findViewById(R.id.tvTotalCases);
        tvTotalTreated = findViewById(R.id.tvTotalTreated);
        tvTotalContacts = findViewById(R.id.tvTotalContacts);
        containerSeverity = findViewById(R.id.containerSeverity);
        containerTreatedBySeverity = findViewById(R.id.containerTreatedBySeverity);
        tvPickedDate = findViewById(R.id.tvPickedDate);
        tvDateCount = findViewById(R.id.tvDateCount);
        com.google.android.material.button.MaterialButton btnPickDate =
                findViewById(R.id.btnPickDate);

        btnPickDate.setOnClickListener(v -> showDatePicker());

        loadStats();
    }

    private void loadStats() {
        try {
            int totalCases = sum(repo.stats().countCasesBySeverity());
            int totalTreated = repo.stats().getTotalTreatedCount();
            int totalContacts = repo.stats().getCustomContactsCount();

            tvTotalCases.setText(String.valueOf(totalCases));
            tvTotalTreated.setText(String.valueOf(totalTreated));
            tvTotalContacts.setText(String.valueOf(totalContacts));

            Map<String, Integer> casesBySeverity = repo.stats().countCasesBySeverity();
            int maxCases = max(casesBySeverity);

            containerSeverity.removeAllViews();
            addBar(containerSeverity, "حرجة",
                    casesBySeverity.getOrDefault("critical", 0), maxCases,
                    R.color.severity_critical);
            addBar(containerSeverity, "متوسطة",
                    casesBySeverity.getOrDefault("medium", 0), maxCases,
                    R.color.severity_medium);
            addBar(containerSeverity, "خفيفة",
                    casesBySeverity.getOrDefault("minor", 0), maxCases,
                    R.color.severity_minor);

            Map<String, Integer> treatedBySeverity = repo.stats().countTreatedBySeverity();
            int maxTreated = max(treatedBySeverity);

            containerTreatedBySeverity.removeAllViews();
            if (treatedBySeverity.isEmpty()) {
                TextView tv = new TextView(this);
                tv.setText(R.string.stats_no_data);
                tv.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
                containerTreatedBySeverity.addView(tv);
            } else {
                addBar(containerTreatedBySeverity, "حرجة",
                        treatedBySeverity.getOrDefault("critical", 0), maxTreated,
                        R.color.severity_critical);
                addBar(containerTreatedBySeverity, "متوسطة",
                        treatedBySeverity.getOrDefault("medium", 0), maxTreated,
                        R.color.severity_medium);
                addBar(containerTreatedBySeverity, "خفيفة",
                        treatedBySeverity.getOrDefault("minor", 0), maxTreated,
                        R.color.severity_minor);
                addBar(containerTreatedBySeverity, "غير مصنّف",
                        treatedBySeverity.getOrDefault("unlinked", 0), maxTreated,
                        R.color.text_secondary);
            }
        } catch (Exception e) {
            android.util.Log.e("StatsActivity", "loadStats failed", e);
            android.widget.Toast.makeText(this, "حدث خطأ في تحميل الإحصاءات",
                    android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * يضيف صفًّا شريطيًا إلى الحاوية.
     */
    private void addBar(LinearLayout parent, String label,
                        int count, int max, int colorRes) {
        View row = LayoutInflater.from(this).inflate(R.layout.item_stat_bar, parent, false);

        View dot = row.findViewById(R.id.dot);
        TextView tvLabel = row.findViewById(R.id.tvLabel);
        TextView tvCount = row.findViewById(R.id.tvCount);
        ProgressBar progress = row.findViewById(R.id.progress);

        int color = ContextCompat.getColor(this, colorRes);

        dot.setBackgroundTintList(ColorStateList.valueOf(color));
        tvLabel.setText(label);
        tvCount.setText(String.valueOf(count));

        int percent = max == 0 ? 0 : (int) Math.round(count * 100.0 / max);
        progress.setProgress(percent);
        progress.setProgressTintList(ColorStateList.valueOf(color));

        parent.addView(row);
    }

    private int sum(Map<String, Integer> map) {
        int s = 0;
        for (Integer v : map.values()) s += v;
        return s;
    }

    private int max(Map<String, Integer> map) {
        int m = 0;
        for (Integer v : map.values()) if (v > m) m = v;
        return m;
    }

    @SuppressLint("SetTextI18n")
    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, y, m, d) -> {
            @SuppressLint("DefaultLocale") String date = String.format("%04d-%02d-%02d", y, m + 1, d);
            tvPickedDate.setText(date);
            int count = repo.stats().getTreatedCountByDate(date);
            tvDateCount.setText(getString(R.string.stats_cases_in_date) + " " + count);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }
}