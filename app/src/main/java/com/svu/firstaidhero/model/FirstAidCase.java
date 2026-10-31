package com.svu.firstaidhero.model;

/**
 * Represents a single row from the 'FirstAidCases' table.
 * This model acts as a data transfer object (DTO) that holds the details
 * of a specific first aid case.
 */
public class FirstAidCase {

    private int id;
    private String title;
    private String symptoms;

    // The severity level of the case: "critical", "medium", or "minor"
    private String severity;

    // Flag to indicate if this case should be displayed in the main "Common Cases" list
    private boolean isCommon;

    /**
     * Default empty constructor required for certain frameworks or serialization.
     */
    public FirstAidCase() { }

    /**
     * Parameterized constructor to initialize all fields of the first aid case.
     *
     * @param id       The unique identifier of the case.
     * @param title    The title of the case (e.g., "Severe Bleeding").
     * @param symptoms The symptoms associated with this case.
     * @param severity The severity level (critical, medium, minor).
     * @param isCommon True if it's a common case, false otherwise.
     */
    public FirstAidCase(int id, String title, String symptoms, String severity, boolean isCommon) {
        this.id = id;
        this.title = title;
        this.symptoms = symptoms;
        this.severity = severity;
        this.isCommon = isCommon;
    }

    // ================= Getters and Setters =================

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public boolean isCommon() {
        return isCommon;
    }

    public void setCommon(boolean common) {
        isCommon = common;
    }

    /**
     * Overrides the default toString method for easier debugging and logging.
     *
     * @return A string representation of the FirstAidCase object.
     */
    @Override
    public String toString() {
        return "FirstAidCase{" + title + ", severity=" + severity + '}';
    }
}