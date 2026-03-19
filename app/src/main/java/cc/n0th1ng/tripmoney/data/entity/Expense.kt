package cc.n0th1ng.tripmoney.data.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "expense")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo("amount") val amount: Double,
    @ColumnInfo("currency") val currency: String,
    @ColumnInfo("note") val note: String,
    @ColumnInfo("datetime") val datetime: String,
    @ColumnInfo("category_id") val categoryId: Int,
    @ColumnInfo("trip_id") val tripId: Int
)

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

