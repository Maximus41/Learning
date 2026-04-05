package com.agn.corea.db.dao

import androidx.room.*
import com.agn.corea.models.subjects.Subject

@Dao
interface SubjectDao {

    @Query("SELECT * FROM subjects")
    suspend fun getAll(): List<Subject>

    @Query("SELECT * FROM subjects WHERE subjectId = :subjectId LIMIT 1")
    fun getBySubjectIdSync(subjectId: String): Subject?

    @Insert
    suspend fun insert(subject: Subject): Long

    @Delete
    suspend fun delete(subject: Subject)
}
