package cc.n0th1ng.tripmoney.data.repository

import androidx.annotation.WorkerThread
import cc.n0th1ng.tripmoney.data.dao.CategoryDao
import cc.n0th1ng.tripmoney.data.entity.Category
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CategoryRepository @Inject constructor(private val categoryDao: CategoryDao) {

    @WorkerThread
    suspend fun save(category: Category) {
        categoryDao.insert(category)
    }

    @WorkerThread
    suspend fun delete(category: Category) {
        categoryDao.delete(category)
    }

    fun getCategories(): Flow<List<Category>> {
        return categoryDao.categories()
    }

    fun getArchivedCategories(): Flow<List<Category>> {
        return categoryDao.archivedCategories()
    }
}