package com.svu.firstaidhero.data.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Data Access Object (DAO) for handling statistical and analytical queries used in reports.
 * Utilizes SQL aggregate functions such as COUNT and GROUP BY.
 */
public class StatsDao {

    private final SQLiteDatabase db;

    public StatsDao(SQLiteDatabase db) {
        this.db = db;
    }

    /**
     * Counts the number of pre-populated first aid cases for each severity level
     * (e.g., critical, medium, minor).
     *
     * @return A Map containing severity levels as keys and their respective counts as values.
     */
    public Map<String, Integer> countCasesBySeverity() {
        Map<String, Integer> map = new LinkedHashMap<>();
        Cursor c = db.rawQuery(
                "SELECT severity, COUNT(*) AS cnt FROM FirstAidCases " +
                        "GROUP BY severity ORDER BY severity",
                null);
        try {
            while (c.moveToNext()) {
                map.put(c.getString(0), c.getInt(1));
            }
        } finally {
            c.close();
        }
        return map;
    }

    /**
     * Counts the number of treated cases the user has logged, grouped by the severity
     * of the original first aid case.
     * Uses a LEFT JOIN to handle cases that might not be linked to a specific FirstAidCase.
     *
     * @return A Map containing severity levels (or 'unlinked') as keys and counts as values.
     */
    public Map<String, Integer> countTreatedBySeverity() {
        Map<String, Integer> map = new LinkedHashMap<>();
        Cursor c = db.rawQuery(
                "SELECT f.severity, COUNT(t.id) AS cnt " +
                        "FROM TreatedCases t " +
                        "LEFT JOIN FirstAidCases f ON t.case_id = f.id " +
                        "GROUP BY f.severity",
                null);
        try {
            while (c.moveToNext()) {
                String key = c.getString(0);
                // If the case is not linked to a FirstAidCase, assign it to 'unlinked'
                if (key == null) key = "unlinked";
                map.put(key, c.getInt(1));
            }
        } finally {
            c.close();
        }
        return map;
    }

    /**
     * Retrieves the total number of treated cases recorded by the user.
     *
     * @return The total count of treated cases.
     */
    public int getTotalTreatedCount() {
        return scalar("SELECT COUNT(*) FROM TreatedCases");
    }

    /**
     * Counts the number of treated cases recorded on a specific date.
     *
     * @param date The date to query (expected format: yyyy-MM-dd).
     * @return The number of cases recorded on the given date.
     */
    public int getTreatedCountByDate(String date) {
        Cursor c = db.rawQuery(
                "SELECT COUNT(*) FROM TreatedCases WHERE date = ?",
                new String[]{date});
        try {
            return c.moveToFirst() ? c.getInt(0) : 0;
        } finally {
            c.close();
        }
    }

    /**
     * Counts the number of custom emergency contacts added by the user.
     *
     * @return The total count of custom contacts.
     */
    public int getCustomContactsCount() {
        return scalar("SELECT COUNT(*) FROM EmergencyContacts WHERE type='custom'");
    }

    // ================= Helper Methods =================

    /**
     * Helper method to execute a query that returns a single integer value (scalar).
     *
     * @param sql The SQL query string.
     * @return The resulting integer value, or 0 if the result set is empty.
     */
    private int scalar(String sql) {
        Cursor c = db.rawQuery(sql, null);
        try {
            return c.moveToFirst() ? c.getInt(0) : 0;
        } finally {
            c.close();
        }
    }
}