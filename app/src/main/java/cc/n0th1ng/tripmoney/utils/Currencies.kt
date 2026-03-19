package cc.n0th1ng.tripmoney.utils

enum class Currencies {
    PLN,
    EUR,
    USD;

    companion object {
        fun default(): Currencies {
            return PLN
        }
        fun names(): List<String> {
            return Currencies.entries.map { it.name }
        }
    }
}