package com.svu.firstaidhero.ui.treated;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.svu.firstaidhero.R;
import com.svu.firstaidhero.data.FirstAidRepository;
import com.svu.firstaidhero.model.TreatedCase;
import com.svu.firstaidhero.ui.adapter.TreatedCaseAdapter;

import java.util.List;

/**
 * Activity responsible for displaying the list of treated cases.
 * Handles search filtering, empty state visualization, and navigation
 * to add, edit, or delete treated cases.
 */
public class TreatedCasesActivity extends AppCompatActivity
        implements TreatedCaseAdapter.Listener {

    // Request codes used when starting the AddTreatedCaseActivity
    public static final int REQ_ADD = 1;
    public static final int REQ_EDIT = 2;

    private FirstAidRepository repo;
    private TreatedCaseAdapter adapter;
    private RecyclerView rvTreated;
    private TextView tvEmpty;
    private EditText etSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_treated_cases);

        repo = FirstAidRepository.getInstance(this);

        // Bind UI components
        ImageView btnBack = findViewById(R.id.btnBack);
        ImageView btnAdd = findViewById(R.id.btnAdd);
        rvTreated = findViewById(R.id.rvTreated);
        tvEmpty = findViewById(R.id.tvEmpty);
        etSearch = findViewById(R.id.etSearch);

        // Setup navigation buttons
        btnBack.setOnClickListener(v -> finish());
        btnAdd.setOnClickListener(v -> {
            Intent i = new Intent(this, AddTreatedCaseActivity.class);
            startActivityForResult(i, REQ_ADD);
        });

        // Setup RecyclerView and Adapter
        adapter = new TreatedCaseAdapter(this);
        rvTreated.setLayoutManager(new LinearLayoutManager(this));
        rvTreated.setAdapter(adapter);

        // Setup real-time search filtering as the user types
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { }

            @Override
            public void afterTextChanged(Editable s) {
                // Reload the list based on the new search query
                load(s.toString().trim());
            }
        });

        // Initial data load
        load("");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the list when returning to this activity (e.g., after adding or editing a case)
        load(etSearch.getText().toString().trim());
    }

    /**
     * Loads treated cases from the database based on the provided search query.
     * Updates the RecyclerView and toggles the empty state visibility.
     *
     * @param query The search term. If empty, retrieves all cases.
     */
    private void load(String query) {
        List<TreatedCase> list;
        if (query.isEmpty()) {
            list = repo.treated().getAll();
        } else {
            list = repo.treated().search(query);
        }

        adapter.setItems(list);

        // Toggle empty state visibility based on the results
        tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
        rvTreated.setVisibility(list.isEmpty() ? View.GONE : View.VISIBLE);
    }

    /**
     * Callback triggered when the user clicks the edit button on a treated case.
     * Launches the AddTreatedCaseActivity in "Edit Mode", passing the case ID.
     *
     * @param t The TreatedCase object to edit.
     */
    @Override
    public void onEdit(TreatedCase t) {
        Intent i = new Intent(this, AddTreatedCaseActivity.class);
        i.putExtra(AddTreatedCaseActivity.EXTRA_ID, t.getId());
        startActivityForResult(i, REQ_EDIT);
    }

    /**
     * Callback triggered when the user clicks the delete button on a treated case.
     * Displays a confirmation dialog before deleting the record from the database.
     *
     * @param t The TreatedCase object to delete.
     */
    @Override
    public void onDelete(TreatedCase t) {
        new AlertDialog.Builder(this)
                .setTitle("حذف الحالة")
                .setMessage(R.string.confirm_delete)
                .setPositiveButton("حذف", (d, w) -> {
                    // Delete from database and refresh the list
                    repo.treated().delete(t.getId());
                    load(etSearch.getText().toString().trim());
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }
}