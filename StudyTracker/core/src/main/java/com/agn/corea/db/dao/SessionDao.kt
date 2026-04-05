package com.agn.corea.db.dao

import androidx.room.*
import com.agn.corea.models.session.Session

@Dao
interface SessionDao {

    @Query("SELECT * FROM sessions WHERE subjectId = :subjectId")
    suspend fun getBySubjectId(subjectId: String?): List<Session>

    @Query("SELECT * FROM sessions WHERE subjectId = :subjectId")
    fun getBySubjectIdSync(subjectId: String): List<Session>

    @Query("SELECT COUNT(*) FROM sessions WHERE subjectId = :subjectId AND startedOn != 0")
    fun countStartedBySubjectId(subjectId: String): Long

    @Query("SELECT * FROM sessions WHERE subjectId = :subjectId AND isSessionActive = 1 LIMIT 1")
    fun getFirstActiveBySubjectIdSync(subjectId: String): Session?

    @Query("SELECT * FROM sessions WHERE sessionId = :sessionId")
    suspend fun getBySessionId(sessionId: String?): List<Session>

    @Insert
    suspend fun insert(session: Session): Long

    @Update
    suspend fun update(session: Session)

    @Delete
    suspend fun delete(session: Session)

    @Delete
    suspend fun deleteList(sessions: List<Session>)
}
