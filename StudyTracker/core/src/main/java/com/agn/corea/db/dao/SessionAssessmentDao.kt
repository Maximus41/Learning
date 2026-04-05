package com.agn.corea.db.dao

import androidx.room.*
import com.agn.corea.models.session.SessionAssessment
import io.reactivex.Single

@Dao
interface SessionAssessmentDao {

    @Query("SELECT * FROM session_assessments WHERE sessionId = :sessionId")
    fun getBySessionId(sessionId: String?): Single<List<SessionAssessment>>

    @Query("SELECT * FROM session_assessments WHERE sessionId = :sessionId LIMIT 1")
    fun getBySessionIdSync(sessionId: String): SessionAssessment?

    @Insert
    fun insert(assessment: SessionAssessment): Long

    @Delete
    fun deleteList(assessments: List<SessionAssessment>)
}
