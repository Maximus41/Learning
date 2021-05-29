
package com.poc.corea.models.session;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.relation.ToMany;

@Entity
public class Next {
    @Id
    public long obId;
    public ToMany<NextSection> nextSections;
}
