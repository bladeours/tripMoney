package cc.n0th1ng.tripmoney.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import cc.n0th1ng.tripmoney.data.entity.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Upsert
    suspend fun insert(category: Category)


    @Transaction
    @Query(
        """
        SELECT * FROM category
    """
    )
    fun categories(): Flow<List<Category>>

}
