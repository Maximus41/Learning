package com.agn.corea.db.dao

import androidx.room.*
import com.agn.corea.models.session.SessionTopic
import io.reactivex.Single

@Dao
interface SessionTopicDao {

    @Query("SELECT * FROM session_topics WHERE sessionId = :sessionId")
    fun getBySessionId(sessionId: String?): Single<List<SessionTopic>>

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
    fun insert(topic: SessionTopic): Long

    @Update
    fun update(topic: SessionTopic)

    @Delete
    fun delete(topic: SessionTopic)

    @Delete
    fun deleteList(topics: List<SessionTopic>)
}
