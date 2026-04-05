package com.agn.corea.db.dao

import androidx.room.*
import com.agn.corea.models.session.SessionTopic

@Dao
interface SessionTopicDao {

    @Query("SELECT * FROM session_topics WHERE sessionId = :sessionId")
    suspend fun getBySessionId(sessionId: String?): List<SessionTopic>

    @Query("SELECT * FROM session_topics WHERE sessionId = :sessionId")
    fun getBySessionIdSync(sessionId: String): List<SessionTopic>

    @Query("SELECT COUNT(*) FROM session_topics WHERE sessionId = :sessionId")
    fun countBySessionId(sessionId: String): Long

    @Query("SELECT * FROM session_topics WHERE sessionId = :sessionId AND sectionTitle = :sectionTitle LIMIT 1")
    fun getBySessionAndSectionTitleSync(sessionId: String, sectionTitle: String): SessionTopic?

    @Query("SELECT * FROM session_topics WHERE topicId = :topicId LIMIT 1")
    fun getByTopicIdSync(topicId: String): SessionTopic?

    @Query("SELECT * FROM session_topics WHERE obId = :obId LIMIT 1")
    fun getByObIdSync(obId: Long): SessionTopic?

    @Insert
    suspend fun insert(topic: SessionTopic): Long

    @Update
    suspend fun update(topic: SessionTopic)

    @Delete
    suspend fun delete(topic: SessionTopic)

    @Delete
    suspend fun deleteList(topics: List<SessionTopic>)
}
