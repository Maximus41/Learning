
package com.poc.corea.models.session;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.relation.ToMany;

@Entity
public class SessionStudyTopics {

    @Id
    public long obId;
    private Integer sectionsCount;
    public ToMany<Section> sections;

    public Integer getSectionsCount() {
        return sectionsCount;
    }

    public void setSectionsCount(Integer sectionsCount) {
        this.sectionsCount = sectionsCount;
    }

}
