package com.agn.corea.db.dao

import androidx.room.*
import com.agn.corea.models.subjects.Subject
import io.reactivex.Single

@Dao
interface SubjectDao {

    @Query("SELECT * FROM subjects")
    fun getAll(): Single<List<Subject>>

    @Query("SELECT * FROM subjects WHERE subjectId = :subjectId LIMIT 1")
    fun getBySubjectIdSync(subjectId: String): Subject?

    @Insert
    fun insert(subject: Subject): Long

    @Delete
    fun delete(subject: Subject)
}
