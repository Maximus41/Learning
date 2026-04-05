package com.agn.corea.db.dao

import androidx.room.*
import com.agn.corea.models.session.summary.PageWiseSessionSummary

@Dao
interface PageWiseSessionSummaryDao {

    @Insert
    suspend fun insert(summary: PageWiseSessionSummary): Long
}
