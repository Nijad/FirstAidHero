package com.svu.firstaidhero.data.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.svu.firstaidhero.model.TreatedCase;

import java.util.ArrayList;
import java.util.List;

/**
 * عمليات كاملة (CRUD) على جدول TreatedCases.
 */
public class TreatedCaseDao {

    private final SQLiteDatabase db;

    public TreatedCaseDao(SQLiteDatabase db) {
        this.db = db;
    }

    // ================= Read =================

    public List<TreatedCase> getAll() {
        return query("SELECT * FROM TreatedCases ORDER BY date DESC, id DESC", null);
    }

    /** بحث عن حالات عولجت في تاريخ معيّن (yyyy-MM-dd). */
    public List<TreatedCase> getByDate(String date) {
        return query("SELECT * FROM TreatedCases WHERE date = ? ORDER BY id DESC",
                new String[]{date});
    }

    /** بحث نصي في الوصف أو الإجراءات. */
    public List<TreatedCase> search(String q) {
        if (q == null) q = "";
        String pattern = "%" + q.trim() + "%";
        return query(
                "SELECT * FROM TreatedCases " +
                        "WHERE description LIKE ? OR procedures LIKE ? " +
                        "ORDER BY date DESC, id DESC",
                new String[]{pattern, pattern});
    }

    public TreatedCase getById(int id) {
        List<TreatedCase> list = query(
                "SELECT * FROM TreatedCases WHERE id=? LIMIT 1",
                new String[]{String.valueOf(id)});
        return list.isEmpty() ? null : list.get(0);
    }

    // ================= Create =================

    public long insert(TreatedCase t) {
        ContentValues v = new ContentValues();
        if (t.getCaseId() != null) v.put("case_id", t.getCaseId());
        else v.putNull("case_id");
        v.put("description", t.getDescription());
        v.put("date", t.getDate());
        v.put("photo_path", t.getPhotoPath());
        v.put("procedures", t.getProcedures());
        return db.insert("TreatedCases", null, v);
    }

    // ================= Update =================

    public int update(TreatedCase t) {
        ContentValues v = new ContentValues();
        if (t.getCaseId() != null) v.put("case_id", t.getCaseId());
        else v.putNull("case_id");
        v.put("description", t.getDescription());
        v.put("date", t.getDate());
        v.put("photo_path", t.getPhotoPath());
        v.put("procedures", t.getProcedures());
        return db.update("TreatedCases", v, "id=?",
                new String[]{String.valueOf(t.getId())});
    }

    // ================= Delete =================

    public int delete(int id) {
        return db.delete("TreatedCases", "id=?", new String[]{String.valueOf(id)});
    }

    // ================= Helper =================

    private List<TreatedCase> query(String sql, String[] args) {
        List<TreatedCase> list = new ArrayList<>();
        Cursor c = db.rawQuery(sql, args);
        try {
            while (c.moveToNext()) list.add(fromCursor(c));
        } finally {
            c.close();
        }
        return list;
    }

    private TreatedCase fromCursor(Cursor c) {
        int caseIdIdx = c.getColumnIndexOrThrow("case_id");
        Integer caseId = c.isNull(caseIdIdx) ? null : c.getInt(caseIdIdx);
        return new TreatedCase(
                c.getInt(c.getColumnIndexOrThrow("id")),
                caseId,
                c.getString(c.getColumnIndexOrThrow("description")),
                c.getString(c.getColumnIndexOrThrow("date")),
                c.getString(c.getColumnIndexOrThrow("photo_path")),
                c.getString(c.getColumnIndexOrThrow("procedures"))
        );
    }
}