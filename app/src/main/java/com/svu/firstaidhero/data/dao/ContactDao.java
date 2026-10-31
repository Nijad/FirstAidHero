package com.svu.firstaidhero.data.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.svu.firstaidhero.model.EmergencyContact;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for handling complete CRUD operations
 * on the 'EmergencyContacts' table.
 */
public class ContactDao {

    private final SQLiteDatabase db;

    public ContactDao(SQLiteDatabase db) {
        this.db = db;
    }

    // ================= Read Operations =================

    /**
     * Retrieves all emergency contacts, sorted by type (general first)
     * and then alphabetically by name.
     *
     * @return List of all EmergencyContact objects.
     */
    public List<EmergencyContact> getAllContacts() {
        return query("SELECT * FROM EmergencyContacts ORDER BY type ASC, name ASC", null);
    }

    /**
     * Retrieves only the general (pre-populated) emergency contacts.
     *
     * @return List of general EmergencyContact objects.
     */
    public List<EmergencyContact> getGeneralContacts() {
        return query("SELECT * FROM EmergencyContacts WHERE type='general' ORDER BY name ASC", null);
    }

    /**
     * Retrieves only the custom emergency contacts added by the user.
     *
     * @return List of custom EmergencyContact objects.
     */
    public List<EmergencyContact> getCustomContacts() {
        return query("SELECT * FROM EmergencyContacts WHERE type='custom' ORDER BY name ASC", null);
    }

    /**
     * Retrieves a specific contact by its unique ID.
     *
     * @param id The unique identifier of the contact.
     * @return The EmergencyContact object, or null if not found.
     */
    public EmergencyContact getById(int id) {
        List<EmergencyContact> list = query(
                "SELECT * FROM EmergencyContacts WHERE id=? LIMIT 1",
                new String[]{String.valueOf(id)});
        return list.isEmpty() ? null : list.get(0);
    }

    // ================= Create Operation =================

    /**
     * Inserts a new contact into the database.
     * Defaults to 'custom' type if no type is provided.
     *
     * @param contact The EmergencyContact object to insert.
     * @return The row ID of the newly inserted contact, or -1 if an error occurred.
     */
    public long insert(EmergencyContact contact) {
        ContentValues v = new ContentValues();
        v.put("name", contact.getName());
        v.put("phone", contact.getPhone());
        v.put("type", contact.getType() != null ? contact.getType() : EmergencyContact.TYPE_CUSTOM);
        v.put("category", contact.getCategory());
        return db.insert("EmergencyContacts", null, v);
    }

    // ================= Update Operation =================

    /**
     * Updates an existing contact in the database based on its ID.
     *
     * @param contact The EmergencyContact object with updated data.
     * @return The number of rows affected.
     */
    public int update(EmergencyContact contact) {
        ContentValues v = new ContentValues();
        v.put("name", contact.getName());
        v.put("phone", contact.getPhone());
        v.put("type", contact.getType());
        v.put("category", contact.getCategory());
        return db.update("EmergencyContacts", v, "id=?",
                new String[]{String.valueOf(contact.getId())});
    }

    // ================= Delete Operation =================

    /**
     * Deletes a custom contact from the database.
     * General contacts cannot be deleted to ensure emergency numbers are always accessible.
     *
     * @param id The ID of the contact to delete.
     * @return The number of rows deleted.
     */
    public int deleteCustom(int id) {
        return db.delete("EmergencyContacts",
                "id=? AND type='custom'",
                new String[]{String.valueOf(id)});
    }

    // ================= Helper Methods =================

    /**
     * Helper method to execute a raw SQL query and map the resulting Cursor
     * to a list of EmergencyContact objects.
     *
     * @param sql  The SQL query string.
     * @param args The arguments for the query (to prevent SQL injection).
     * @return A list of EmergencyContact objects.
     */
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
            // Always close the cursor to prevent memory leaks
            c.close();
        }
        return list;
    }
}