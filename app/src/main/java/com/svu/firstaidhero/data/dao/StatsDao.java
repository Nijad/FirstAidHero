package com.svu.firstaidhero.data.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * استعلامات إحصائية للتقارير.
 * يُستخدم COUNT + GROUP BY.
 */
public class StatsDao {

    private final SQLiteDatabase db;

    public StatsDao(SQLiteDatabase db) {
        this.db = db;
    }

    /** عدد الحالات في كل درجة خطورة (من جدول الحالات المسبقة). */
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

    /** عدد الحالات التي عالجها المستخدم لكل درجة خطورة. */
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
                if (key == null) key = "unlinked";
                map.put(key, c.getInt(1));
            }
        } finally {
            c.close();
        }
        return map;
    }

    /** إجمالي عدد الحالات المسجّلة. */
    public int getTotalTreatedCount() {
        return scalar("SELECT COUNT(*) FROM TreatedCases");
    }

    /** عدد الحالات المُسجّلة في تاريخ معيّن. */
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

    /** عدد الأرقام المخصصة التي أضافها المستخدم. */
    public int getCustomContactsCount() {
        return scalar("SELECT COUNT(*) FROM EmergencyContacts WHERE type='custom'");
    }

    // ================= Helper =================

    private int scalar(String sql) {
        Cursor c = db.rawQuery(sql, null);
        try {
            return c.moveToFirst() ? c.getInt(0) : 0;
        } finally {
            c.close();
        }
    }
}