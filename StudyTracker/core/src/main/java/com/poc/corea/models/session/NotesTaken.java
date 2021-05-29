
package com.poc.corea.models.session;

public class NotesTaken {

    private String stat;
    private Integer statId;
    private String date;
    private float takeNotesStoryPoints;

    public String getStat() {
        return stat;
    }

    public void setStat(String stat) {
        this.stat = stat;
    }

    public Integer getStatId() {
        return statId;
    }

    public void setStatId(Integer statId) {
        this.statId = statId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public float getTakeNotesStoryPoints() {
        return takeNotesStoryPoints;
    }

    public void setTakeNotesStoryPoints(float takeNotesStoryPoints) {
        this.takeNotesStoryPoints = takeNotesStoryPoints;
    }
}
