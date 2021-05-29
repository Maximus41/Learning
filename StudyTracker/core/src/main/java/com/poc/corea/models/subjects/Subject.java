
package com.poc.corea.models.subjects;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Unique;
import io.objectbox.relation.ToMany;

@Entity
public class Subject {

    @Id
    public long obId;
    @Unique
    public String subjectId;
    public String learningId;
    public String summaryId;
    public String subjectTitle;
    public ToMany<SubjectSection> subjectSections;

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public String getLearningId() {
        return learningId;
    }

    public void setLearningId(String learningId) {
        this.learningId = learningId;
    }

    public String getSummaryId() {
        return summaryId;
    }

    public void setSummaryId(String summaryId) {
        this.summaryId = summaryId;
    }

    public String getSubjectTitle() {
        return subjectTitle;
    }

    public void setSubjectTitle(String subjectTitle) {
        this.subjectTitle = subjectTitle;
    }
}
