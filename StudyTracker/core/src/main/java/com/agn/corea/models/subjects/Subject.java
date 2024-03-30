package com.agn.corea.models.subjects;

import java.util.UUID;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Unique;
import io.objectbox.relation.ToMany;

@Entity
public class Subject {
    @Id
    public long obId;
    @Unique
    private String subjectId = UUID.randomUUID().toString();
    public String subjectTitle;
    public long createdOn;
    public int nosOfSections;
    public ToMany<Section> sections;

    public String getSubjectId() {
        return subjectId;
    }
}
