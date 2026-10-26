package com.svu.firstaidhero.model;

/**
 * يمثّل رقم طوارئ (عام أو مخصص) من جدول EmergencyContacts.
 */
public class EmergencyContact {

    public static final String TYPE_GENERAL = "general";
    public static final String TYPE_CUSTOM  = "custom";

    private int id;
    private String name;
    private String phone;
    private String type;      // general | custom
    private String category;  // ambulance | police | fire | red_crescent | doctor | family

    public EmergencyContact() { }

    public EmergencyContact(int id, String name, String phone, String type, String category) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.type = type;
        this.category = category;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public boolean isGeneral() { return TYPE_GENERAL.equals(type); }
    public boolean isCustom()  { return TYPE_CUSTOM.equals(type);  }
}