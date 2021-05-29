
package com.poc.corea.models.summary;

public class CumulativeStatus {

    private Read read;
    private NotesTaken notesTaken;
    private Memorized memorized;
    private Reviewed reviewed;
    private Practiced practiced;

    public Read getRead() {
        return read;
    }

    public void setRead(Read read) {
        this.read = read;
    }

    public NotesTaken getNotesTaken() {
        return notesTaken;
    }

    public void setNotesTaken(NotesTaken notesTaken) {
        this.notesTaken = notesTaken;
    }

    public Memorized getMemorized() {
        return memorized;
    }

    public void setMemorized(Memorized memorized) {
        this.memorized = memorized;
    }

    public Reviewed getReviewed() {
        return reviewed;
    }

    public void setReviewed(Reviewed reviewed) {
        this.reviewed = reviewed;
    }

    public Practiced getPracticed() {
        return practiced;
    }

    public void setPracticed(Practiced practiced) {
        this.practiced = practiced;
    }

}
