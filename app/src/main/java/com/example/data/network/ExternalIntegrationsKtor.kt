package com.example.data.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// ---------------------------------------------------------
// Stripe Payment Intents & Billing API
// ---------------------------------------------------------
@Serializable
data class StripePaymentRequest(val amount: Long, val currency: String = "usd", val customerId: String)
@Serializable
data class StripePaymentResponse(val clientSecret: String, val status: String)

// ---------------------------------------------------------
// Twilio Transactional SMS API
// ---------------------------------------------------------
@Serializable
data class TwilioSmsRequest(val to: String, val from: String, val body: String)
@Serializable
data class TwilioSmsResponse(val sid: String, val status: String)

// ---------------------------------------------------------
// Zendesk SLA Escalations API
// ---------------------------------------------------------
@Serializable
data class ZendeskTicketRequest(val ticket: ZendeskTicketPayload)
@Serializable
data class ZendeskTicketPayload(val subject: String, val comment: ZendeskComment, val priority: String = "urgent")
@Serializable
data class ZendeskComment(val body: String)
@Serializable
data class ZendeskTicketResponse(val ticket: ZendeskTicketPayload)

// ---------------------------------------------------------
// DocuSign Embedded Signing API
// ---------------------------------------------------------
@Serializable
data class DocuSignEnvelopeRequest(val templateId: String, val emailSubject: String, val status: String = "sent")
@Serializable
data class DocuSignEnvelopeResponse(val envelopeId: String, val uri: String)

// ---------------------------------------------------------
// Algolia Unified Search Indexing API
// ---------------------------------------------------------
@Serializable
data class AlgoliaSearchRequest(val params: String)
@Serializable
data class AlgoliaSearchResponse(val hits: List<Map<String, String>>)

class ExternalIntegrationsClient {

    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend fun createStripePaymentIntent(authHeader: String, request: StripePaymentRequest): HttpResponse {
        return client.post("https://api.stripe.com/v1/payment_intents") {
            header("Authorization", authHeader)
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    suspend fun sendTwilioSms(accountSid: String, authHeader: String, request: TwilioSmsRequest): HttpResponse {
        return client.post("https://api.twilio.com/2010-04-01/Accounts/$accountSid/Messages.json") {
            header("Authorization", authHeader)
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    suspend fun createZendeskTicket(authHeader: String, request: ZendeskTicketRequest): HttpResponse {
        return client.post("https://your-domain.zendesk.com/api/v2/tickets.json") {
            header("Authorization", authHeader)
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    suspend fun createDocuSignEnvelope(accountId: String, authHeader: String, request: DocuSignEnvelopeRequest): HttpResponse {
        return client.post("https://demo.docusign.net/restapi/v2.1/accounts/$accountId/envelopes") {
            header("Authorization", authHeader)
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    suspend fun downloadS3Asset(url: String, authHeader: String): HttpResponse {
        return client.get(url) {
            header("Authorization", authHeader)
        }
    }

    suspend fun queryAlgolia(indexName: String, apiKey: String, appId: String, request: AlgoliaSearchRequest): HttpResponse {
        return client.post("https://your-app-id-dsn.algolia.net/1/indexes/$indexName/query") {
            header("X-Algolia-API-Key", apiKey)
            header("X-Algolia-Application-Id", appId)
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }
}
