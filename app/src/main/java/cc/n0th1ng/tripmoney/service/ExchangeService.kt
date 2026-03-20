package cc.n0th1ng.tripmoney.service

import cc.n0th1ng.tripmoney.utils.Currencies
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.json.JSONObject
import java.time.LocalDate
import javax.inject.Inject

class ExchangeService @Inject() constructor() {
    private val API_URL: String = "https://api.frankfurter.dev"
    private val client = HttpClient()

    suspend fun getRate(base: Currencies, target: Currencies, date: LocalDate): Double {
        return try {
            val response = client.get("$API_URL/v1/$date") {
                url {
                    parameters.append("base", base.name)
                    parameters.append("symbols", target.name)
                }
            }
            val json = Json
            json.parseToJsonElement(response.bodyAsText()).jsonObject["rates"]?.jsonObject[target.name]?.jsonPrimitive?.double
                ?: throw Exception("can not find rates")
        } catch (e: Exception) {
            throw IllegalStateException("Error fetching exchange rate: ${e.message}")
        }
    }
}