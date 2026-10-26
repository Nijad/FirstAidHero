package com.svu.firstaidhero.model;

/**
 * يمثّل حالة عالجها المستخدم من جدول TreatedCases.
 * case_id اختياري (قد يكون NULL).
 */
public class TreatedCase {

    private int id;
    private Integer caseId;      // nullable
    private String description;
    private String date;         // ISO: yyyy-MM-dd
    private String photoPath;    // nullable
    private String procedures;

    public TreatedCase() { }

    public TreatedCase(int id, Integer caseId, String description,
                       String date, String photoPath, String procedures) {
        this.id = id;
        this.caseId = caseId;
        this.description = description;
        this.date = date;
        this.photoPath = photoPath;
        this.procedures = procedures;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Integer getCaseId() { return caseId; }
    public void setCaseId(Integer caseId) { this.caseId = caseId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getPhotoPath() { return photoPath; }
    public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }

    public String getProcedures() { return procedures; }
    public void setProcedures(String procedures) { this.procedures = procedures; }
}