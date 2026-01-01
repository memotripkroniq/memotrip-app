package com.example.memotrip_kroniq.data.tripmap

import com.example.memotrip_kroniq.ui.addtrip.TransportType
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
        transport: TransportType
    ): String {

        val response: GenerateTripMapResponse =
            client.post("https://memotrip-bff-production.up.railway.app/trips/render-map") {
                contentType(ContentType.Application.Json)
                setBody(
                    GenerateTripMapRequest(
                        from = from,
                        to = to,
                        transports = listOf(transport.name)
                    )
                )
            }.body()

        return response.imageUrl
    }
}
