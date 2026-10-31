package com.svu.firstaidhero.data.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.svu.firstaidhero.model.TreatedCase;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for handling complete CRUD operations
 * on the 'TreatedCases' table.
 */
public class TreatedCaseDao {

    private final SQLiteDatabase db;

    public TreatedCaseDao(SQLiteDatabase db) {
        this.db = db;
    }

    // ================= Read Operations =================

    /**
     * Retrieves all treated cases from the database.
     * Sorted by date descending, then by ID descending (newest first).
     *
     * @return List of all TreatedCase objects.
     */
    public List<TreatedCase> getAll() {
        return query("SELECT * FROM TreatedCases ORDER BY date DESC, id DESC", null);
    }

    /**
     * Searches for treated cases recorded on a specific date.
     *
     * @param date The target date (expected format: yyyy-MM-dd).
     * @return List of TreatedCase objects for that date.
     */
    public List<TreatedCase> getByDate(String date) {
        return query("SELECT * FROM TreatedCases WHERE date = ? ORDER BY id DESC",
                new String[]{date});
    }

    /**
     * Performs a text search within the case description or procedures.
     *
     * @param q The search query string.
     * @return List of TreatedCase objects matching the search term.
     */
    public List<TreatedCase> search(String q) {
        if (q == null) q = "";
        String pattern = "%" + q.trim() + "%";
        return query(
                "SELECT * FROM TreatedCases " +
                        "WHERE description LIKE ? OR procedures LIKE ? " +
                        "ORDER BY date DESC, id DESC",
                new String[]{pattern, pattern});
    }

    /**
     * Retrieves a specific treated case by its unique ID.
     *
     * @param id The unique identifier of the treated case.
     * @return The TreatedCase object, or null if not found.
     */
    public TreatedCase getById(int id) {
        List<TreatedCase> list = query(
                "SELECT * FROM TreatedCases WHERE id=? LIMIT 1",
                new String[]{String.valueOf(id)});
        return list.isEmpty() ? null : list.get(0);
    }

    // ================= Create Operation =================

    /**
     * Inserts a new treated case into the database.
     * Handles nullable 'case_id' by setting it to NULL if not provided.
     *
     * @param t The TreatedCase object to insert.
     * @return The row ID of the newly inserted case, or -1 if an error occurred.
     */
    public long insert(TreatedCase t) {
        ContentValues v = new ContentValues();
        // Allow case_id to be null if the user logs a case not pre-defined in the app
        if (t.getCaseId() != null) v.put("case_id", t.getCaseId());
        else v.putNull("case_id");
        v.put("description", t.getDescription());
        v.put("date", t.getDate());
        v.put("photo_path", t.getPhotoPath());
        v.put("procedures", t.getProcedures());
        return db.insert("TreatedCases", null, v);
    }

    // ================= Update Operation =================

    /**
     * Updates an existing treated case in the database based on its ID.
     *
     * @param t The TreatedCase object with updated data.
     * @return The number of rows affected.
     */
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

    // ================= Delete Operation =================

    /**
     * Deletes a treated case from the database by its ID.
     *
     * @param id The ID of the treated case to delete.
     * @return The number of rows deleted.
     */
    public int delete(int id) {
        return db.delete("TreatedCases", "id=?", new String[]{String.valueOf(id)});
    }

    // ================= Helper Methods =================

    /**
     * Helper method to execute a raw SQL query and map the results
     * to a list of TreatedCase objects.
     *
     * @param sql  The SQL query string.
     * @param args The arguments for the query.
     * @return A list of TreatedCase objects.
     */
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

    /**
     * Helper method to map a database Cursor row to a TreatedCase object.
     * Safely handles a null 'case_id' column.
     *
     * @param c The Cursor pointing to the current row.
     * @return A populated TreatedCase object.
     */
    private TreatedCase fromCursor(Cursor c) {
        int caseIdIdx = c.getColumnIndexOrThrow("case_id");
        // Handle nullable case_id
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