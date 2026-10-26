package com.svu.firstaidhero.data.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.svu.firstaidhero.model.EmergencyContact;

import java.util.ArrayList;
import java.util.List;

/**
 * عمليات كاملة (CRUD) على جدول EmergencyContacts.
 */
public class ContactDao {

    private final SQLiteDatabase db;

    public ContactDao(SQLiteDatabase db) {
        this.db = db;
    }

    // ================= Read =================

    public List<EmergencyContact> getAllContacts() {
        return query("SELECT * FROM EmergencyContacts ORDER BY type ASC, name ASC", null);
    }

    public List<EmergencyContact> getGeneralContacts() {
        return query("SELECT * FROM EmergencyContacts WHERE type='general' ORDER BY name ASC", null);
    }

    public List<EmergencyContact> getCustomContacts() {
        return query("SELECT * FROM EmergencyContacts WHERE type='custom' ORDER BY name ASC", null);
    }

    public EmergencyContact getById(int id) {
        List<EmergencyContact> list = query(
                "SELECT * FROM EmergencyContacts WHERE id=? LIMIT 1",
                new String[]{String.valueOf(id)});
        return list.isEmpty() ? null : list.get(0);
    }

    // ================= Create =================

    public long insert(EmergencyContact contact) {
        ContentValues v = new ContentValues();
        v.put("name", contact.getName());
        v.put("phone", contact.getPhone());
        v.put("type", contact.getType() != null ? contact.getType() : EmergencyContact.TYPE_CUSTOM);
        v.put("category", contact.getCategory());
        return db.insert("EmergencyContacts", null, v);
    }

    // ================= Update =================

    public int update(EmergencyContact contact) {
        ContentValues v = new ContentValues();
        v.put("name", contact.getName());
        v.put("phone", contact.getPhone());
        v.put("type", contact.getType());
        v.put("category", contact.getCategory());
        return db.update("EmergencyContacts", v, "id=?",
                new String[]{String.valueOf(contact.getId())});
    }

    // ================= Delete =================

    /** حذف رقم مخصص فقط (لا نسمح بحذف الأرقام العامة). */
    public int deleteCustom(int id) {
        return db.delete("EmergencyContacts",
                "id=? AND type='custom'",
                new String[]{String.valueOf(id)});
    }

    // ================= Helper =================

    private List<EmergencyContact> query(String sql, String[] args) {
        List<EmergencyContact> list = new ArrayList<>();
        Cursor c = db.rawQuery(sql, args);
        try {
            while (c.moveToNext()) {
                EmergencyContact e = new EmergencyContact(
                        c.getInt(c.getColumnIndexOrThrow("id")),
                        c.getString(c.getColumnIndexOrThrow("name")),
                        c.getString(c.getColumnIndexOrThrow("phone")),
                        c.getString(c.getColumnIndexOrThrow("type")),
                        c.getString(c.getColumnIndexOrThrow("category"))
                );
                list.add(e);
            }
        } finally {
            c.close();
        }
        return list;
    }
}