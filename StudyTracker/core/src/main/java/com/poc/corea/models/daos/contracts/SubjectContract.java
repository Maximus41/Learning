package com.poc.corea.models.daos.contracts;

import com.poc.corea.models.filtercriteria.Criteria;
import com.poc.corea.models.subjects.Subject;

import java.util.List;

public class SubjectContract {

    public static interface SubjectDao {
        void createSubject(Subject subject);
        void updateSubject(Subject subject);
        void deleteSubject(String subjectId);
        void getSubjectById(String subjectId);
        void getSubjectByOdId(long id);
        List<Subject> getAllSubjects();
        List<Subject> filterSubjects(Criteria criteria);
    }

}
