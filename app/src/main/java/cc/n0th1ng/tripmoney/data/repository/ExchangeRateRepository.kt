package cc.n0th1ng.tripmoney.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.WorkerThread
import cc.n0th1ng.tripmoney.data.dao.CategoryDao
import cc.n0th1ng.tripmoney.data.dao.ExchangeRateDao
import cc.n0th1ng.tripmoney.data.entity.Category
import cc.n0th1ng.tripmoney.data.entity.ExchangeRate
import cc.n0th1ng.tripmoney.service.ExchangeService
import cc.n0th1ng.tripmoney.utils.Currencies
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class ExchangeRateRepository @Inject constructor(
    private val exchangeRateDao: ExchangeRateDao,
    private val exchangeService: ExchangeService
) {

    @WorkerThread
    suspend fun save(exchangeRate: ExchangeRate) {
        exchangeRateDao.insert(exchangeRate)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getRate(base: Currencies, target: Currencies, date: LocalDate): Double {
        if(base == target) return 1.0
        val id = ExchangeRate.buildKey(base.name, target.name, date.toString())
        val cachedRate = exchangeRateDao.getById(id)
        return if (cachedRate != null) {
            cachedRate.rate
        } else {
            val rate = exchangeService.getRate(base, target, date)
            exchangeRateDao.insert(
                ExchangeRate(
                    id = id,
                    base = base.name,
                    target = target.name,
                    rate = rate,
                    date = date.toString()
                )
            )
            rate
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun clearOldRates(daysToKeep: Int = 180) {
        val cutoffDate = LocalDate.now().minusDays(daysToKeep.toLong()).toString()
        exchangeRateDao.deleteOldRates(cutoffDate)
    }
}