package com.svu.firstaidhero.model;

/**
 * يمثّل صفًا واحدًا من جدول FirstAidCases.
 */
public class FirstAidCase {

    private int id;
    private String title;
    private String symptoms;
    private String severity;   // critical | medium | minor
    private boolean isCommon;

    public FirstAidCase() { }

    public FirstAidCase(int id, String title, String symptoms, String severity, boolean isCommon) {
        this.id = id;
        this.title = title;
        this.symptoms = symptoms;
        this.severity = severity;
        this.isCommon = isCommon;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSymptoms() { return symptoms; }
    public void setSymptoms(String symptoms) { this.symptoms = symptoms; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public boolean isCommon() { return isCommon; }
    public void setCommon(boolean common) { isCommon = common; }

    @Override
    public String toString() {
        return "FirstAidCase{" + title + ", severity=" + severity + '}';
    }
}