package cc.n0th1ng.tripmoney.data.entity

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import cc.n0th1ng.tripmoney.utils.Currencies
import java.time.LocalDate

@Entity(tableName = "trip")
data class Trip(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo("name") val name: String,
    @ColumnInfo("start_date") val startDate: LocalDate,
    @ColumnInfo("currency") val currency: String
){
    companion object {
        @RequiresApi(Build.VERSION_CODES.O)
        val DUMMY = Trip(-1, "dummy", LocalDate.now(), Currencies.default().name)
    }
}