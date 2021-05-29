package com.poc.corea.models.daos.contracts;

import com.poc.corea.models.summary.SummarySubject;

public class SummaryContract {

    public static interface SubjectDao {
        SummarySubject getSubjects();
    }

}
