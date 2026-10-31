package com.svu.firstaidhero.ui.details;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.svu.firstaidhero.R;
import com.svu.firstaidhero.data.FirstAidRepository;
import com.svu.firstaidhero.model.ActionStep;
import com.svu.firstaidhero.model.FirstAidCase;

import java.util.List;

/**
 * Activity responsible for displaying the details of a specific first aid case.
 * Shows the case title, symptoms, severity badge, and a dynamically populated
 * list of action steps.
 */
public class CaseDetailsActivity extends AppCompatActivity {

    // Intent extra key used to pass the case ID to this activity
    public static final String EXTRA_CASE_ID = "extra_case_id";

    private FirstAidRepository repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_case_details);

        repo = FirstAidRepository.getInstance(this);

        // Retrieve the case ID from the Intent
        int caseId = getIntent().getIntExtra(EXTRA_CASE_ID, -1);

        // Exit gracefully if the case ID is invalid
        if (caseId == -1) {
            finish();
            return;
        }

        // Fetch the case data from the database
        FirstAidCase c = repo.cases().getCaseById(caseId);

        // Exit gracefully if the case was not found (e.g., deleted)
        if (c == null) {
            finish();
            return;
        }

        // ===== Bind UI Components =====
        ImageView btnBack = findViewById(R.id.btnBack);
        TextView tvToolbarTitle = findViewById(R.id.tvToolbarTitle);
        TextView tvSeverityBadge = findViewById(R.id.tvSeverityBadge);
        TextView tvSymptoms = findViewById(R.id.tvSymptoms);
        LinearLayout containerSteps = findViewById(R.id.containerSteps);

        // Setup back button click listener
        btnBack.setOnClickListener(v -> finish());

        // Populate basic case information
        tvToolbarTitle.setText(c.getTitle());
        tvSymptoms.setText(c.getSymptoms());

        // ===== Setup Severity Badge =====
        // Determine the color and string resource based on the severity level
        int colorRes;
        int labelRes = switch (c.getSeverity()) {
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

        // Apply the color and text to the severity badge
        int color = ContextCompat.getColor(this, colorRes);
        tvSeverityBadge.setBackgroundTintList(ColorStateList.valueOf(color));
        tvSeverityBadge.setText(labelRes);

        // ===== Populate Action Steps Dynamically =====
        // Note: We use a LinearLayout inside a ScrollView instead of a RecyclerView
        // to prevent text clipping issues with long Arabic text.
        List<ActionStep> steps = repo.cases().getStepsForCase(caseId);
        LayoutInflater inflater = LayoutInflater.from(this);

        for (ActionStep step : steps) {
            // Inflate the step item layout
            View stepView = inflater.inflate(R.layout.item_step, containerSteps, false);
            TextView tvNumber = stepView.findViewById(R.id.tvStepNumber);
            TextView tvInstruction = stepView.findViewById(R.id.tvInstruction);

            // Bind data to the step view
            tvNumber.setText(String.valueOf(step.getStepNumber()));
            tvInstruction.setText(step.getInstruction());

            // Add the step view to the container
            containerSteps.addView(stepView);
        }
    }
}