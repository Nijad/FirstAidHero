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

public class TreatedCasesActivity extends AppCompatActivity
        implements TreatedCaseAdapter.Listener {

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

        ImageView btnBack = findViewById(R.id.btnBack);
        ImageView btnAdd = findViewById(R.id.btnAdd);
        rvTreated = findViewById(R.id.rvTreated);
        tvEmpty = findViewById(R.id.tvEmpty);
        etSearch = findViewById(R.id.etSearch);

        btnBack.setOnClickListener(v -> finish());
        btnAdd.setOnClickListener(v -> {
            Intent i = new Intent(this, AddTreatedCaseActivity.class);
            startActivityForResult(i, REQ_ADD);
        });

        adapter = new TreatedCaseAdapter(this);
        rvTreated.setLayoutManager(new LinearLayoutManager(this));
        rvTreated.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { }
            @Override
            public void afterTextChanged(Editable s) {
                load(s.toString().trim());
            }
        });

        load("");
    }

    @Override
    protected void onResume() {
        super.onResume();
        load(etSearch.getText().toString().trim());
    }

    private void load(String query) {
        List<TreatedCase> list;
        if (query.isEmpty()) {
            list = repo.treated().getAll();
        } else {
            list = repo.treated().search(query);
        }
        adapter.setItems(list);

        tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
        rvTreated.setVisibility(list.isEmpty() ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onEdit(TreatedCase t) {
        Intent i = new Intent(this, AddTreatedCaseActivity.class);
        i.putExtra(AddTreatedCaseActivity.EXTRA_ID, t.getId());
        startActivityForResult(i, REQ_EDIT);
    }

    @Override
    public void onDelete(TreatedCase t) {
        new AlertDialog.Builder(this)
                .setTitle("حذف الحالة")
                .setMessage(R.string.confirm_delete)
                .setPositiveButton("حذف", (d, w) -> {
                    repo.treated().delete(t.getId());
                    load(etSearch.getText().toString().trim());
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }
}