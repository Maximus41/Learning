package com.agn.corea.db.dao

import androidx.room.*
import com.agn.corea.models.session.summary.SessionSummary

@Dao
interface SessionSummaryDao {

    @Insert
    suspend fun insert(summary: SessionSummary): Long

    @Update
    suspend fun update(summary: SessionSummary)
}
