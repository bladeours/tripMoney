package cc.n0th1ng.tripmoney.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import cc.n0th1ng.tripmoney.data.entity.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Upsert
    suspend fun insert(category: Category)


    @Delete
    suspend fun delete(category: Category)

    @Transaction
    @Query(
        """
        SELECT * FROM category WHERE archived is 0 ORDER BY name
    """
    )
    fun categories(): Flow<List<Category>>

    @Transaction
    @Query(
        """
        SELECT * FROM category WHERE archived is 1
    """
    )
    fun archivedCategories(): Flow<List<Category>>

}
