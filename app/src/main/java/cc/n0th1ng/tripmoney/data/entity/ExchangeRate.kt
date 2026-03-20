package cc.n0th1ng.tripmoney.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("exchange_rate")
data class ExchangeRate(
    @PrimaryKey
    val id: String,
    val base: String,
    val target: String,
    val rate: Double,
    val date: String
) {
    companion object {
        fun buildKey(base: String, target: String, date: String): String {
            return "${base}_${target}_${date}"
        }
    }
}
