package cc.n0th1ng.tripmoney.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
fun LocalDate.pretty(): String {
    return this.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
}