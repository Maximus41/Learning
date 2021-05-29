
package com.poc.corea.models.session;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.relation.ToOne;

@Entity
public class NextSectionPages {
    @Id
    public long obId;
    public ToOne<NextSectionPage> nextSectionFirstPage;
    public ToOne<NextSectionPage> nextSectionSecondPage;
}
