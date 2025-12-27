package com.example.memotrip_kroniq.data.location

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.parameter
import io.ktor.client.request.url
import io.ktor.http.HttpHeaders
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class LocationSearchRepository(
    private val client: HttpClient
) {

    suspend fun search(query: String): List<LocationSuggestion> {
        if (query.length < 3) return emptyList()

        val response: List<NominatimResponse> = client.get {
            url("https://nominatim.openstreetmap.org/search")
            parameter("q", query)
            parameter("format", "json")
            parameter("limit", 5)
            headers {
                append(
                    HttpHeaders.UserAgent,
                    "MemoTripKroniQ/1.0 (contact@memotrip.app)"
                )
            }
        }.body()

        return response.map {
            LocationSuggestion(
                displayName = it.displayName,
                lat = it.lat.toDouble(),
                lon = it.lon.toDouble()
            )
        }
    }
}

@Serializable
private data class NominatimResponse(
    @SerialName("display_name") val displayName: String,
    val lat: String,
    val lon: String
)
