package com.svu.firstaidhero.model;

/**
 * Represents an emergency contact (either general or custom)
 * from the 'EmergencyContacts' table.
 */
public class EmergencyContact {

    // Constant for pre-populated, non-deletable emergency contacts
    public static final String TYPE_GENERAL = "general";

    // Constant for user-added, editable/deletable emergency contacts
    public static final String TYPE_CUSTOM  = "custom";

    private int id;
    private String name;
    private String phone;

    // Type of the contact: "general" or "custom"
    private String type;

    // Specific category: "ambulance" | "police" | "fire" | "red_crescent" | "doctor" | "family"
    private String category;

    /**
     * Default empty constructor required for certain frameworks or serialization.
     */
    public EmergencyContact() { }

    /**
     * Parameterized constructor to initialize all fields of the emergency contact.
     *
     * @param id       The unique identifier of the contact.
     * @param name     The name of the contact (e.g., "Ambulance", "Dr. Ahmad").
     * @param phone    The phone number.
     * @param type     The type of contact (general or custom).
     * @param category The category (e.g., ambulance, doctor).
     */
    public EmergencyContact(int id, String name, String phone, String type, String category) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.type = type;
        this.category = category;
    }

    // ================= Getters and Setters =================

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    // ================= Helper Methods =================

    /**
     * Checks if the contact is a general emergency contact.
     *
     * @return true if the type is 'general', false otherwise.
     */
    public boolean isGeneral() {
        return TYPE_GENERAL.equals(type);
    }

    /**
     * Checks if the contact is a custom user-added contact.
     *
     * @return true if the type is 'custom', false otherwise.
     */
    public boolean isCustom() {
        return TYPE_CUSTOM.equals(type);
    }
}