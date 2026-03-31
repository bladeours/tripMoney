package cc.n0th1ng.tripmoney.data.entity

import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import cc.n0th1ng.tripmoney.utils.Icons

@Entity(tableName = "category")
@Immutable
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo("name") val name: String,
    @ColumnInfo("icon") val icon: Icons,
    @ColumnInfo("color") val color: String,
    @ColumnInfo("archived") val archived: Boolean = false
)