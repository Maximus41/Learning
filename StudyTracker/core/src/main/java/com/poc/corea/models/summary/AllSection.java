
package com.poc.corea.models.summary;

import com.poc.corea.models.obconverters.SectionStatusConverter;

import io.objectbox.annotation.Convert;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Unique;
import io.objectbox.relation.ToMany;

@Entity
public class AllSection {

    @Id
    public long obId;

    @Unique
    private String sectionId;
    private String sectionName;

    @Convert(converter = SectionStatusConverter.class, dbType = String.class)
    private SectionStatus sectionStatus;
    public ToMany<SummaryPage> summaryPages;

    public String getSectionId() {
        return sectionId;
    }

    public void setSectionId(String sectionId) {
        this.sectionId = sectionId;
    }

    public String getSectionName() {
        return sectionName;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }

    public SectionStatus getSectionStatus() {
        return sectionStatus;
    }

    public void setSectionStatus(SectionStatus sectionStatus) {
        this.sectionStatus = sectionStatus;
    }

}
