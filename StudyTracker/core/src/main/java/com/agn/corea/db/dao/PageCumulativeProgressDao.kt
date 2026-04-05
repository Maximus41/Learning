package com.agn.corea.db.dao

import androidx.room.*
import com.agn.corea.models.subjects.PageCumulativeProgress

@Dao
interface PageCumulativeProgressDao {

    @Query("SELECT * FROM page_cumulative_progress WHERE pageId = :pageId LIMIT 1")
    fun getByPageIdSync(pageId: String): PageCumulativeProgress?

    @Query("SELECT * FROM page_cumulative_progress WHERE pageId IN (:pageIds)")
    suspend fun getByPageIds(pageIds: List<String>): List<PageCumulativeProgress>

    @Insert
    suspend fun insert(progress: PageCumulativeProgress): Long

    @Update
    suspend fun update(progress: PageCumulativeProgress)

    @Delete
    suspend fun delete(progress: PageCumulativeProgress)

    @Delete
    suspend fun deleteList(progressList: List<PageCumulativeProgress>)
}
