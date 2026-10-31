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

/**
 * Main entry point of the application.
 * Displays a list of common first aid cases, provides an instant search bar,
 * and allows navigation to other features (Contacts, Treated Cases, Statistics).
 */
public class MainActivity extends AppCompatActivity {

    private FirstAidRepository repo;
    private CaseAdapter adapter;

    // UI Components
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

        // Bind UI components
        etSearch = findViewById(R.id.etSearch);
        btnClear = findViewById(R.id.btnClearSearch);
        tvSectionTitle = findViewById(R.id.tvSectionTitle);
        tvEmpty = findViewById(R.id.tvEmpty);
        rvCases = findViewById(R.id.rvCases);

        // Setup RecyclerView and Adapter
        adapter = new CaseAdapter(this::onCaseClicked);
        rvCases.setLayoutManager(new LinearLayoutManager(this));
        rvCases.setAdapter(adapter);

        // Setup real-time search functionality
        setupSearch();

        // Load default list of common cases
        loadCommonCases();

        // ===== Setup Navigation Buttons =====
        findViewById(R.id.btnContacts).setOnClickListener(v -> startActivity(new android.content.Intent(this,
                com.svu.firstaidhero.ui.contacts.ContactsActivity.class)));

        findViewById(R.id.btnTreated).setOnClickListener(v -> startActivity(new android.content.Intent(this,
                com.svu.firstaidhero.ui.treated.TreatedCasesActivity.class)));

        findViewById(R.id.btnStats).setOnClickListener(v -> startActivity(new android.content.Intent(this,
                com.svu.firstaidhero.ui.stats.StatsActivity.class)));

        // ===== Setup Floating Action Button (FAB) =====
        // Navigates directly to the Emergency Contacts screen for quick access
        com.google.android.material.floatingactionbutton.FloatingActionButton fabEmergencyCall = findViewById(R.id.fabEmergencyCall);
        fabEmergencyCall.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(this, com.svu.firstaidhero.ui.contacts.ContactsActivity.class);
            startActivity(intent);
        });

        // Force the entire layout to support Right-To-Left (RTL) for Arabic language
        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
    }

    /**
     * Initializes the search bar with a TextWatcher to perform instant filtering.
     * Toggles the visibility of the clear (X) button and updates the section title.
     */
    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) { }
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) { }

            @Override
            public void afterTextChanged(Editable s) {
                String q = s.toString().trim();

                // Show clear button only if there is text
                btnClear.setVisibility(q.isEmpty() ? View.GONE : View.VISIBLE);

                if (q.isEmpty()) {
                    // Revert to default view if search is empty
                    tvSectionTitle.setText(R.string.common_cases_title);
                    loadCommonCases();
                } else {
                    // Perform search if text is present
                    tvSectionTitle.setText("نتائج البحث");
                    performSearch(q);
                }
            }
        });

        // Clear search input when the X button is clicked
        btnClear.setOnClickListener(v -> etSearch.setText(""));
    }

    /**
     * Loads the pre-defined common cases from the database into the RecyclerView.
     */
    private void loadCommonCases() {
        List<FirstAidCase> list = repo.cases().getCommonCases();
        adapter.setItems(list);
        updateEmpty(list.isEmpty(), getString(R.string.no_cases));
    }

    /**
     * Executes a database search based on the provided query.
     * Wrapped in a try-catch block to ensure the app doesn't crash on unexpected DB errors.
     *
     * @param query The search term entered by the user.
     */
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

    /**
     * Toggles the visibility between the RecyclerView and the empty state TextView.
     *
     * @param show    True to show the empty state, false to show the list.
     * @param message The message to display in the empty state.
     */
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

    /**
     * Callback triggered when a case is clicked in the RecyclerView.
     * Navigates to the CaseDetailsActivity, passing the selected case ID.
     *
     * @param c The selected FirstAidCase object.
     */
    private void onCaseClicked(FirstAidCase c) {
        android.content.Intent i = new android.content.Intent(this,
                com.svu.firstaidhero.ui.details.CaseDetailsActivity.class);
        i.putExtra(com.svu.firstaidhero.ui.details.CaseDetailsActivity.EXTRA_CASE_ID, c.getId());
        startActivity(i);
    }
}