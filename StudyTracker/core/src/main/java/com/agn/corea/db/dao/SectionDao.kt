package com.agn.corea.db.dao

import androidx.room.*
import com.agn.corea.models.subjects.Section
import kotlinx.coroutines.flow.Flow

@Dao
interface SectionDao {

    @Query("SELECT * FROM sections WHERE subjectId = :subjectId")
    suspend fun getBySubjectId(subjectId: String?): List<Section>

    @Query("SELECT * FROM sections WHERE subjectId = :subjectId")
    fun observeBySubjectId(subjectId: String?): Flow<List<Section>>

    @Query("SELECT * FROM sections WHERE subjectId = :subjectId")
    fun getBySubjectIdSync(subjectId: String): List<Section>

    @Query("SELECT * FROM sections WHERE sectionId = :sectionId LIMIT 1")
    fun getBySectionIdSync(sectionId: String): Section?

    @Query("SELECT * FROM sections WHERE sectionTitle = :title AND subjectId = :subjectId LIMIT 1")
    fun getByTitleAndSubjectSync(title: String, subjectId: String): Section?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(section: Section): Long

    @Update
    suspend fun update(section: Section)

    @Delete
    suspend fun deleteList(sections: List<Section>)
}
