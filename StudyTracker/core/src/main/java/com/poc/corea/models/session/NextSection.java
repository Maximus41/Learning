
package com.poc.corea.models.session;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Unique;
import io.objectbox.relation.ToOne;

@Entity
public class NextSection {

    @Id
    public long obId;
    @Unique
    private String nextSectionId;
    private String nextSectionName;
    public ToOne<NextSectionPages> nextSectionPages;

    public String getNextSectionId() {
        return nextSectionId;
    }

    public void setNextSectionId(String nextSectionId) {
        this.nextSectionId = nextSectionId;
    }

    public String getNextSectionName() {
        return nextSectionName;
    }

    public void setNextSectionName(String nextSectionName) {
        this.nextSectionName = nextSectionName;
    }
}
