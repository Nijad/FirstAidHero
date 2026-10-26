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
import com.svu.firstaidhero.ui.adapter.StepAdapter;

import java.util.List;

public class CaseDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_CASE_ID = "extra_case_id";

    private FirstAidRepository repo;
    private StepAdapter stepAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_case_details);

        repo = FirstAidRepository.getInstance(this);

        // استقبال معرف الحالة
        int caseId = getIntent().getIntExtra(EXTRA_CASE_ID, -1);
        if (caseId == -1) {
            finish();
            return;
        }

        FirstAidCase c = repo.cases().getCaseById(caseId);
        if (c == null) {
            finish();
            return;
        }

        // ربط الواجهات
        ImageView btnBack = findViewById(R.id.btnBack);
        TextView tvToolbarTitle = findViewById(R.id.tvToolbarTitle);
        TextView tvSeverityBadge = findViewById(R.id.tvSeverityBadge);
        TextView tvSymptoms = findViewById(R.id.tvSymptoms);
//        RecyclerView rvSteps = findViewById(R.id.rvSteps);

        btnBack.setOnClickListener(v -> finish());

        tvToolbarTitle.setText(c.getTitle());
        tvSymptoms.setText(c.getSymptoms());

        // شارة الخطورة
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
        int color = ContextCompat.getColor(this, colorRes);
        tvSeverityBadge.setBackgroundTintList(ColorStateList.valueOf(color));
        tvSeverityBadge.setText(labelRes);

        LinearLayout containerSteps = findViewById(R.id.containerSteps);
        List<ActionStep> steps = repo.cases().getStepsForCase(caseId);
        LayoutInflater inflater = LayoutInflater.from(this);

        for (ActionStep step : steps) {
            View stepView = inflater.inflate(R.layout.item_step, containerSteps, false);
            TextView tvNumber = stepView.findViewById(R.id.tvStepNumber);
            TextView tvInstruction = stepView.findViewById(R.id.tvInstruction);

            tvNumber.setText(String.valueOf(step.getStepNumber()));
            tvInstruction.setText(step.getInstruction());

            containerSteps.addView(stepView);
        }
    }
}