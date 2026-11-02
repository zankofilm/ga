package ir.madreseyar.guardian

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class ApiException(
    val status: Int,
    override val message: String,
    val code: String = "",
    val payload: JSONObject? = null,
) : Exception(message)

object ApiClient {
    @Volatile var token: String = ""

    suspend fun get(path: String, auth: Boolean = true): JSONObject = request(path, "GET", null, auth)
    suspend fun post(path: String, body: JSONObject = JSONObject(), auth: Boolean = true): JSONObject = request(path, "POST", body, auth)

    private fun open(path: String, method: String, auth: Boolean): HttpURLConnection {
        val base = BuildConfig.API_BASE_URL.trimEnd('/')
        val url = URL(base + if (path.startsWith('/')) path else "/$path")
        return (url.openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 6_000
            readTimeout = 12_000
            useCaches = false
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "MadreseyarGuardianAndroid/${BuildConfig.VERSION_NAME}")
            if (auth && token.isNotBlank()) setRequestProperty("Authorization", "Bearer $token")
        }
    }

    private suspend fun request(path: String, method: String, body: JSONObject?, auth: Boolean): JSONObject = withContext(Dispatchers.IO) {
        val conn = open(path, method, auth).apply {
            if (body != null) {
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
            }
        }
        try {
            if (body != null) conn.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            val status = conn.responseCode
            val stream = if (status in 200..299) conn.inputStream else conn.errorStream
            val text = stream?.let { input -> BufferedReader(InputStreamReader(input, Charsets.UTF_8)).use { it.readText() } }.orEmpty()
            val json = runCatching { if (text.isBlank()) JSONObject() else JSONObject(text) }
                .getOrElse { JSONObject().put("error", text.ifBlank { "HTTP $status" }) }
            if (status !in 200..299) throw ApiException(status, json.optString("error", "خطای $status"), json.optString("code"), json)
            json
        } finally { conn.disconnect() }
    }
}
