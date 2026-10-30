package com.svu.firstaidhero.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.svu.firstaidhero.R;
import com.svu.firstaidhero.data.FirstAidRepository;
import com.svu.firstaidhero.model.FirstAidCase;
import com.svu.firstaidhero.ui.adapter.CaseAdapter;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FirstAidRepository repo;
    private CaseAdapter adapter;

    private EditText etSearch;
    private ImageView btnClear;
    private TextView tvSectionTitle;
    private TextView tvEmpty;
    private RecyclerView rvCases;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        repo = FirstAidRepository.getInstance(this);

        etSearch = findViewById(R.id.etSearch);
        btnClear = findViewById(R.id.btnClearSearch);
        tvSectionTitle = findViewById(R.id.tvSectionTitle);
        tvEmpty = findViewById(R.id.tvEmpty);
        rvCases = findViewById(R.id.rvCases);

        // إعداد القائمة
        adapter = new CaseAdapter(this::onCaseClicked);
        rvCases.setLayoutManager(new LinearLayoutManager(this));
        rvCases.setAdapter(adapter);

        // إعداد البحث
        setupSearch();

        // تحميل الحالات الشائعة افتراضيًا
        loadCommonCases();

        findViewById(R.id.btnContacts).setOnClickListener(v -> startActivity(new android.content.Intent(this,
                com.svu.firstaidhero.ui.contacts.ContactsActivity.class)));

        findViewById(R.id.btnTreated).setOnClickListener(v -> startActivity(new android.content.Intent(this,
                com.svu.firstaidhero.ui.treated.TreatedCasesActivity.class)));

        findViewById(R.id.btnStats).setOnClickListener(v -> startActivity(new android.content.Intent(this,
                com.svu.firstaidhero.ui.stats.StatsActivity.class)));

        // إعداد الزر العائم لفتح صفحة أرقام الطوارئ
        com.google.android.material.floatingactionbutton.FloatingActionButton fabEmergencyCall = findViewById(R.id.fabEmergencyCall);
        fabEmergencyCall.setOnClickListener(v -> {
            // الانتقال إلى صفحة جهات الاتصال
            android.content.Intent intent = new android.content.Intent(this, com.svu.firstaidhero.ui.contacts.ContactsActivity.class);
            startActivity(intent);
        });

        // إجبار التطبيق على RTL
        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) { }
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) { }

            @Override
            public void afterTextChanged(Editable s) {
                String q = s.toString().trim();
                btnClear.setVisibility(q.isEmpty() ? View.GONE : View.VISIBLE);
                if (q.isEmpty()) {
                    tvSectionTitle.setText(R.string.common_cases_title);
                    loadCommonCases();
                } else {
                    tvSectionTitle.setText("نتائج البحث");
                    performSearch(q);
                }
            }
        });

        btnClear.setOnClickListener(v -> etSearch.setText(""));
    }

    private void loadCommonCases() {
        List<FirstAidCase> list = repo.cases().getCommonCases();
        adapter.setItems(list);
        updateEmpty(list.isEmpty(), getString(R.string.no_cases));
    }

    private void performSearch(String query) {
        if (query == null || query.trim().isEmpty()) {
            loadCommonCases();
            return;
        }
        try {
            List<FirstAidCase> list = repo.cases().searchCases(query);
            adapter.setItems(list);
            updateEmpty(list.isEmpty(), getString(R.string.no_results));
        } catch (Exception e) {
            Log.e("MainActivity", "Search failed", e);
            adapter.setItems(java.util.Collections.emptyList());
            updateEmpty(true, "حدث خطأ أثناء البحث");
        }
    }

    private void updateEmpty(boolean show, String message) {
        if (show) {
            tvEmpty.setText(message);
            tvEmpty.setVisibility(View.VISIBLE);
            rvCases.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvCases.setVisibility(View.VISIBLE);
        }
    }

    private void onCaseClicked(FirstAidCase c) {
        android.content.Intent i = new android.content.Intent(this,
                com.svu.firstaidhero.ui.details.CaseDetailsActivity.class);
        i.putExtra(com.svu.firstaidhero.ui.details.CaseDetailsActivity.EXTRA_CASE_ID, c.getId());
        startActivity(i);
    }
}