package com.svu.firstaidhero.model;

/**
 * يمثّل خطوة إسعاف واحدة من جدول ActionSteps.
 */
public class ActionStep {

    private int id;
    private int stepNumber;
    private String instruction;

    public ActionStep(int id, int stepNumber, int step_number, String instruction) {
        this.id = id;
        this.stepNumber = stepNumber;
        this.instruction = instruction;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public void setCaseId(int caseId) {
    }

    public int getStepNumber() { return stepNumber; }
    public void setStepNumber(int stepNumber) { this.stepNumber = stepNumber; }

    public String getInstruction() { return instruction; }
    public void setInstruction(String instruction) { this.instruction = instruction; }
}