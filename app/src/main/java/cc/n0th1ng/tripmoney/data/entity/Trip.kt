package cc.n0th1ng.tripmoney.data.entity

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import cc.n0th1ng.tripmoney.utils.Currencies
import java.time.LocalDate

@Entity(tableName = "trip")
@Immutable
data class Trip(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo("name") val name: String,
    @ColumnInfo("start_date") val startDate: LocalDate,
    @ColumnInfo("end_date") val endDate: LocalDate,
    @ColumnInfo("currency") val currency: String,
    @ColumnInfo("budget") val budget: Double = 0.0
) {
    fun isDummy(): Boolean {
        return this.id == -1
    }

    companion object {
        @RequiresApi(Build.VERSION_CODES.O)
        val DUMMY = Trip(
            -1,
            "",
            LocalDate.now(),
            endDate = LocalDate.now(), Currencies.default().name, budget = 0.0,
        )
    }
}