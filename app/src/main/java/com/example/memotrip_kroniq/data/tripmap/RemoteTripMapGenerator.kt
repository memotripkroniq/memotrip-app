package com.example.memotrip_kroniq.data.tripmap

import com.memotrip_kroniq.BuildConfig
import com.example.memotrip_kroniq.ui.core.model.TransportType
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class RemoteTripMapGenerator(
    private val client: HttpClient
) : TripMapGenerator {

    override suspend fun generate(
        from: String,
        to: String,
        transport: TransportType,
        stops: List<String>
    ): String {

        val response: GenerateTripMapResponse =
            client.post("${BuildConfig.BASE_URL}trips/render-map") {
                contentType(ContentType.Application.Json)
                setBody(
                    GenerateTripMapRequest(
                        from = from,
                        to = to,
                        transports = listOf(transport.name),
                        stops = stops
                    )
                )
            }.body()

        return response.imageUrl
    }
}
