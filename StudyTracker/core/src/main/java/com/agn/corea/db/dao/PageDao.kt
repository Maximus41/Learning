package com.agn.corea.db.dao

import androidx.room.*
import com.agn.corea.models.subjects.Page
import io.reactivex.Single

@Dao
interface PageDao {

    @Query("SELECT * FROM pages WHERE sectionId = :sectionId")
    fun getBySectionIdSync(sectionId: String): List<Page>

    @Query("SELECT * FROM pages WHERE pageId = :pageId LIMIT 1")
    fun getByPageIdSync(pageId: String): Page?

    @Query("SELECT * FROM pages WHERE pageTitle = :title AND sectionId = :sectionId LIMIT 1")
    fun getByTitleAndSectionSync(title: String, sectionId: String): Page?

    @Insert
    fun insert(page: Page): Long

    @Update
    fun update(page: Page)

    @Delete
    fun delete(page: Page)

    @Delete
    fun deleteList(pages: List<Page>)
}
