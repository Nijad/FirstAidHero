package com.svu.firstaidhero.model;

/**
 * Represents a case treated by the user, mapping to the 'TreatedCases' table.
 * The 'caseId' is optional (nullable) because the user might log a case
 * that is not pre-defined in the application's database.
 */
public class TreatedCase {

    private int id;

    // Nullable foreign key linking to the FirstAidCases table
    private Integer caseId;

    // User's description of the incident
    private String description;

    // Date of the incident (format: yyyy-MM-dd)
    private String date;

    // Local file path to the attached photo (nullable)
    private String photoPath;

    // The procedures/actions taken by the user
    private String procedures;

    /**
     * Default empty constructor required for certain frameworks or serialization.
     */
    public TreatedCase() { }

    /**
     * Parameterized constructor to initialize all fields of the treated case.
     *
     * @param id          The unique identifier of the treated case.
     * @param caseId      The optional ID of the linked first aid case (can be null).
     * @param description A brief description of what happened.
     * @param date        The date of the incident (yyyy-MM-dd).
     * @param photoPath   The local path to the saved photo (can be null).
     * @param procedures  The actions or procedures performed by the user.
     */
    public TreatedCase(int id, Integer caseId, String description,
                       String date, String photoPath, String procedures) {
        this.id = id;
        this.caseId = caseId;
        this.description = description;
        this.date = date;
        this.photoPath = photoPath;
        this.procedures = procedures;
    }

    // ================= Getters and Setters =================

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getCaseId() {
        return caseId;
    }

    public void setCaseId(Integer caseId) {
        this.caseId = caseId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public String getProcedures() {
        return procedures;
    }

    public void setProcedures(String procedures) {
        this.procedures = procedures;
    }
}