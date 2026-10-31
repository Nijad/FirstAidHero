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

/**
 * Activity responsible for displaying the analytical dashboard and statistics.
 * Shows total counts, severity distributions as custom bar charts,
 * and allows filtering treated cases by a specific date.
 */
public class StatsActivity extends AppCompatActivity {

    private FirstAidRepository repo;

    // UI Components for totals
    private TextView tvTotalCases, tvTotalTreated, tvTotalContacts;

    // Containers for dynamically generated bar charts
    private LinearLayout containerSeverity, containerTreatedBySeverity;

    // UI Components for date filtering
    private TextView tvPickedDate, tvDateCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        repo = FirstAidRepository.getInstance(this);

        // Setup back button
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Bind UI components
        tvTotalCases = findViewById(R.id.tvTotalCases);
        tvTotalTreated = findViewById(R.id.tvTotalTreated);
        tvTotalContacts = findViewById(R.id.tvTotalContacts);
        containerSeverity = findViewById(R.id.containerSeverity);
        containerTreatedBySeverity = findViewById(R.id.containerTreatedBySeverity);
        tvPickedDate = findViewById(R.id.tvPickedDate);
        tvDateCount = findViewById(R.id.tvDateCount);

        com.google.android.material.button.MaterialButton btnPickDate = findViewById(R.id.btnPickDate);
        btnPickDate.setOnClickListener(v -> showDatePicker());

        // Load statistical data
        loadStats();
    }

    /**
     * Fetches statistical data from the database and populates the UI.
     * Wrapped in a try-catch block to ensure the app doesn't crash
     * if an unexpected database error occurs (Zero Crashes policy).
     */
    private void loadStats() {
        try {
            // ===== 1. Load Totals =====
            int totalCases = sum(repo.stats().countCasesBySeverity());
            int totalTreated = repo.stats().getTotalTreatedCount();
            int totalContacts = repo.stats().getCustomContactsCount();

            tvTotalCases.setText(String.valueOf(totalCases));
            tvTotalTreated.setText(String.valueOf(totalTreated));
            tvTotalContacts.setText(String.valueOf(totalContacts));

            // ===== 2. Load Cases by Severity =====
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

            // ===== 3. Load Treated Cases by Severity =====
            Map<String, Integer> treatedBySeverity = repo.stats().countTreatedBySeverity();
            int maxTreated = max(treatedBySeverity);

            containerTreatedBySeverity.removeAllViews();
            if (treatedBySeverity.isEmpty()) {
                // Display a placeholder text if there is no data
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
                // Handle cases that were logged without being linked to a predefined case
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
     * Dynamically inflates and adds a custom horizontal bar row to a parent LinearLayout.
     * Calculates the percentage relative to the maximum value to determine the progress bar fill.
     *
     * @param parent   The container to add the bar into.
     * @param label    The text label for the row (e.g., "Critical").
     * @param count    The actual value to display.
     * @param max      The maximum value among all categories (used to calculate percentage).
     * @param colorRes The color resource ID to tint the dot and progress bar.
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

        // Calculate the percentage (0 to 100) for the ProgressBar
        int percent = max == 0 ? 0 : (int) Math.round(count * 100.0 / max);
        progress.setProgress(percent);
        progress.setProgressTintList(ColorStateList.valueOf(color));

        parent.addView(row);
    }

    /**
     * Helper method to calculate the sum of all values in a Map.
     *
     * @param map The input map containing integer values.
     * @return The total sum.
     */
    private int sum(Map<String, Integer> map) {
        int s = 0;
        for (Integer v : map.values()) s += v;
        return s;
    }

    /**
     * Helper method to find the maximum value in a Map.
     *
     * @param map The input map containing integer values.
     * @return The maximum integer value, or 0 if the map is empty.
     */
    private int max(Map<String, Integer> map) {
        int m = 0;
        for (Integer v : map.values()) if (v > m) m = v;
        return m;
    }

    /**
     * Displays a DatePicker dialog to allow the user to select a specific date.
     * Fetches and displays the number of treated cases recorded on that date.
     */
    @SuppressLint("SetTextI18n")
    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, y, m, d) -> {
            @SuppressLint("DefaultLocale") String date = String.format("%04d-%02d-%02d", y, m + 1, d);
            tvPickedDate.setText(date);

            // Query the database for the count of cases on the selected date
            int count = repo.stats().getTreatedCountByDate(date);
            tvDateCount.setText(getString(R.string.stats_cases_in_date) + " " + count);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }
}