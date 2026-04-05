package com.agn.corea.db.dao

import androidx.room.*
import com.agn.corea.models.subjects.PageCumulativeProgress
import io.reactivex.Single

@Dao
interface PageCumulativeProgressDao {

    @Query("SELECT * FROM page_cumulative_progress WHERE pageId = :pageId LIMIT 1")
    fun getByPageIdSync(pageId: String): PageCumulativeProgress?

    @Query("SELECT * FROM page_cumulative_progress WHERE pageId IN (:pageIds)")
    fun getByPageIds(pageIds: List<String>): Single<List<PageCumulativeProgress>>

    @Insert
    fun insert(progress: PageCumulativeProgress): Long

    @Update
    fun update(progress: PageCumulativeProgress)

    @Delete
    fun delete(progress: PageCumulativeProgress)

    @Delete
    fun deleteList(progressList: List<PageCumulativeProgress>)
}
