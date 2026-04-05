package com.agn.corea.db.dao

import androidx.room.*
import com.agn.corea.models.session.Session
import io.reactivex.Single

@Dao
interface SessionDao {

    @Query("SELECT * FROM sessions WHERE subjectId = :subjectId")
    fun getBySubjectId(subjectId: String?): Single<List<Session>>

    @Query("SELECT * FROM sessions WHERE subjectId = :subjectId")
    fun getBySubjectIdSync(subjectId: String): List<Session>

    @Query("SELECT COUNT(*) FROM sessions WHERE subjectId = :subjectId AND startedOn != 0")
    fun countStartedBySubjectId(subjectId: String): Long

    @Query("SELECT * FROM sessions WHERE subjectId = :subjectId AND isSessionActive = 1 LIMIT 1")
    fun getFirstActiveBySubjectIdSync(subjectId: String): Session?

    @Query("SELECT * FROM sessions WHERE sessionId = :sessionId")
    fun getBySessionId(sessionId: String?): Single<List<Session>>

    @Insert
    fun insert(session: Session): Long

    @Update
    fun update(session: Session)

    @Delete
    fun delete(session: Session)

    @Delete
    fun deleteList(sessions: List<Session>)
}
