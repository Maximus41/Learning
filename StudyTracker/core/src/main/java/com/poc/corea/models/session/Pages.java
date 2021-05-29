
package com.poc.corea.models.session;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.relation.ToOne;

@Entity
public class Pages {

    @Id
    public long obId;
    public ToOne<SessionPage> firstPage;
    public ToOne<SessionPage> secondPage;
}
