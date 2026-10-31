package com.svu.firstaidhero.model;

/**
 * Represents a single first aid action step from the 'ActionSteps' table.
 * Each step belongs to a specific first aid case and includes an instruction.
 */
public class ActionStep {

    private int id;
    private int caseId; // Foreign key linking to FirstAidCases
    private int stepNumber;
    private String instruction;

    /**
     * Constructs a new ActionStep object.
     *
     * @param id          The unique identifier of the step.
     * @param caseId      The ID of the parent first aid case.
     * @param stepNumber  The sequential number of the step (e.g., 1, 2, 3).
     * @param instruction The actual first aid instruction text.
     */
    public ActionStep(int id, int caseId, int stepNumber, String instruction) {
        this.id = id;
        this.caseId = caseId;
        this.stepNumber = stepNumber;
        this.instruction = instruction;
    }

    // ================= Getters and Setters =================

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCaseId() {
        return caseId;
    }

    public void setCaseId(int caseId) {
        this.caseId = caseId;
    }

    public int getStepNumber() {
        return stepNumber;
    }

    public void setStepNumber(int stepNumber) {
        this.stepNumber = stepNumber;
    }

    public String getInstruction() {
        return instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }
}