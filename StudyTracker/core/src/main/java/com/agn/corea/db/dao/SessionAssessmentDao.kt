package com.agn.corea.db.dao

import androidx.room.*
import com.agn.corea.models.session.SessionAssessment

@Dao
interface SessionAssessmentDao {

    @Query("SELECT * FROM session_assessments WHERE sessionId = :sessionId")
    suspend fun getBySessionId(sessionId: String?): List<SessionAssessment>

    @Query("SELECT * FROM session_assessments WHERE sessionId = :sessionId LIMIT 1")
    fun getBySessionIdSync(sessionId: String): SessionAssessment?

    @Insert
    suspend fun insert(assessment: SessionAssessment): Long

    @Delete
    suspend fun deleteList(assessments: List<SessionAssessment>)
}
