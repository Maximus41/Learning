
package com.poc.corea.models.subjects;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.relation.ToMany;
//Todo:Remove this class.This is redundant
@Entity
public class Subjects {

    @Id
    public long obId;

    public ToMany<Subject> subjects;

}
