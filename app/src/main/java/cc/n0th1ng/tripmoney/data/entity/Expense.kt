package cc.n0th1ng.tripmoney.data.entity

import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.time.LocalDateTime

@Entity(
    tableName = "expense",
    foreignKeys = [ForeignKey(
        entity = Category::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("category_id"),
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["category_id"])]
)
@Immutable
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo("amount") val amount: Double,
    @ColumnInfo("currency") val currency: String,
    @ColumnInfo("note") val note: String,
    @ColumnInfo("datetime") val datetime: LocalDateTime,
    @ColumnInfo("category_id") val categoryId: Int,
    @ColumnInfo("trip_id") val tripId: Int,
    @ColumnInfo("rate") val rate: Double = 1.0
) {
    fun convertedAmount(): Double {
        return this.amount * this.rate
    }
}

data class ExpenseDto(
    @Embedded val expense: Expense,
    @Relation(
        parentColumn = "category_id",
        entityColumn = "id"
    )
    val category: Category,
    @Relation(
        parentColumn = "trip_id",
        entityColumn = "id"
    )
    val trip: Trip
)

