package com.svu.firstaidhero.data.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.svu.firstaidhero.model.ActionStep;
import com.svu.firstaidhero.model.FirstAidCase;

import java.util.ArrayList;
import java.util.List;

/**
 * كل عمليات القراءة والكتابة على جدولي:
 * - FirstAidCases
 * - ActionSteps
 */
public class CaseDao {

    private final SQLiteDatabase db;

    public CaseDao(SQLiteDatabase db) {
        this.db = db;
    }

    // ================= FirstAidCases =================

    /** جميع الحالات (الأكثر شيوعًا أولاً). */
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

    /** الحالات الشائعة فقط (للقائمة الافتراضية في الشاشة الرئيسية). */
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
     * بحث فوري ذكي: يطابق العنوان أو الأعراض أو الكلمات المفتاحية.
     */
    public List<FirstAidCase> searchCases(String query) {
        List<FirstAidCase> list = new ArrayList<>();
        if (query == null) query = "";
        query = query.trim();

        if (query.isEmpty()) {
            return getAllCases();
        }

        // نُجرّب أكثر من نمط: الكلمة كما هي، ومع "ال" بادئة، وبدونها
        String base = query;
        if (base.startsWith("ال") && base.length() > 2) {
            base = base.substring(2);  // "الحروق" → "حروق"
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

    /** جلب حالة واحدة بالمعرّف. */
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

    /** جميع خطوات حالة معيّنة، مرتبة تصاعديًا. */
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

    // ================= مساعد =================

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