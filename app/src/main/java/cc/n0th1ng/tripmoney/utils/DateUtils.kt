package cc.n0th1ng.tripmoney.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun LocalDate.pretty(): String {
    return this.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
}