package com.svu.firstaidhero.data.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.svu.firstaidhero.model.ActionStep;
import com.svu.firstaidhero.model.FirstAidCase;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for handling all read/write operations
 * related to the 'FirstAidCases' and 'ActionSteps' tables.
 */
public class CaseDao {

    private final SQLiteDatabase db;

    public CaseDao(SQLiteDatabase db) {
        this.db = db;
    }

    // ================= FirstAidCases =================

    /**
     * Retrieves all first aid cases from the database.
     * Common cases (is_common = 1) are prioritized, then sorted alphabetically by title.
     *
     * @return List of all FirstAidCase objects.
     */
    public List<FirstAidCase> getAllCases() {
        List<FirstAidCase> list = new ArrayList<>();
        try (Cursor c = db.rawQuery(
                "SELECT id, title, symptoms, severity, is_common " +
                        "FROM FirstAidCases ORDER BY is_common DESC, title ASC",
                null)) {
            while (c.moveToNext()) list.add(fromCursor(c));
        }
        return list;
    }

    /**
     * Retrieves only the most common first aid cases.
     * Used for the default list displayed on the main screen.
     *
     * @return List of common FirstAidCase objects.
     */
    public List<FirstAidCase> getCommonCases() {
        List<FirstAidCase> list = new ArrayList<>();
        try (Cursor c = db.rawQuery(
                "SELECT id, title, symptoms, severity, is_common " +
                        "FROM FirstAidCases WHERE is_common = 1 ORDER BY title ASC",
                null)) {
            while (c.moveToNext()) list.add(fromCursor(c));
        }
        return list;
    }

    /**
     * Performs an instant smart search across title, symptoms, and keywords.
     * Handles Arabic prefixes (like "ال") to improve search accuracy.
     *
     * @param query The search term entered by the user.
     * @return List of FirstAidCase objects matching the query.
     */
    public List<FirstAidCase> searchCases(String query) {
        List<FirstAidCase> list = new ArrayList<>();
        if (query == null) query = "";
        query = query.trim();

        // Return all cases if the search query is empty
        if (query.isEmpty()) {
            return getAllCases();
        }

        // Prepare different patterns to handle the Arabic definite article "ال"
        String base = query;
        if (base.startsWith("ال") && base.length() > 2) {
            base = base.substring(2);  // Example: "الحروق" becomes "حروق"
        }
        String patternRaw = "%" + query + "%";
        String patternNoAl = "%" + base + "%";
        String patternWithAl = "%ال" + base + "%";

        try (Cursor c = db.rawQuery(
                "SELECT id, title, symptoms, severity, is_common " +
                        "FROM FirstAidCases " +
                        "WHERE title    LIKE ? OR title    LIKE ? OR title    LIKE ? " +
                        "   OR symptoms LIKE ? OR symptoms LIKE ? OR symptoms LIKE ? " +
                        "   OR keywords LIKE ? OR keywords LIKE ? OR keywords LIKE ? " +
                        "ORDER BY is_common DESC, title ASC",
                new String[]{
                        patternRaw, patternNoAl, patternWithAl,
                        patternRaw, patternNoAl, patternWithAl,
                        patternRaw, patternNoAl, patternWithAl
                })) {
            while (c.moveToNext()) list.add(fromCursor(c));
        }
        return list;
    }

    /**
     * Retrieves a specific first aid case by its unique ID.
     *
     * @param id The unique identifier of the case.
     * @return The FirstAidCase object, or null if not found.
     */
    public FirstAidCase getCaseById(int id) {
        try (Cursor c = db.rawQuery(
                "SELECT id, title, symptoms, severity, is_common " +
                        "FROM FirstAidCases WHERE id = ? LIMIT 1",
                new String[]{String.valueOf(id)})) {
            if (c.moveToFirst()) return fromCursor(c);
            return null;
        }
    }

    // ================= ActionSteps =================

    /**
     * Retrieves all action steps for a specific case, ordered by step number.
     *
     * @param caseId The ID of the parent case.
     * @return List of ActionStep objects.
     */
    public List<ActionStep> getStepsForCase(int caseId) {
        List<ActionStep> list = new ArrayList<>();
        try (Cursor c = db.rawQuery(
                "SELECT id, case_id, step_number, instruction " +
                        "FROM ActionSteps WHERE case_id = ? ORDER BY step_number ASC",
                new String[]{String.valueOf(caseId)})) {
            while (c.moveToNext()) {
                ActionStep s = new ActionStep(
                        c.getInt(c.getColumnIndexOrThrow("id")),
                        c.getInt(c.getColumnIndexOrThrow("case_id")),
                        c.getInt(c.getColumnIndexOrThrow("step_number")),
                        c.getString(c.getColumnIndexOrThrow("instruction"))
                );
                list.add(s);
            }
        }
        return list;
    }

    // ================= Helper Methods =================

    /**
     * Helper method to map a database Cursor row to a FirstAidCase object.
     *
     * @param c The Cursor pointing to the current row.
     * @return A populated FirstAidCase object.
     */
    private FirstAidCase fromCursor(Cursor c) {
        return new FirstAidCase(
                c.getInt(c.getColumnIndexOrThrow("id")),
                c.getString(c.getColumnIndexOrThrow("title")),
                c.getString(c.getColumnIndexOrThrow("symptoms")),
                c.getString(c.getColumnIndexOrThrow("severity")),
                c.getInt(c.getColumnIndexOrThrow("is_common")) == 1
        );
    }
}