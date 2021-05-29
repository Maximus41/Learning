
package com.poc.corea.models.session;

public class Read {
    private String stat;
    private Integer statId;
    private String date;
    private float readStoryPoints;

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

    public float getReadStoryPoints() {
        return readStoryPoints;
    }

    public void setReadStoryPoints(float readStoryPoints) {
        this.readStoryPoints = readStoryPoints;
    }
}
