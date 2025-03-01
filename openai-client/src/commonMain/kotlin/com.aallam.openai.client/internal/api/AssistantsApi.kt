package com.aallam.openai.client.internal.api

import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.assistant.Assistant
import com.aallam.openai.api.assistant.AssistantFile
import com.aallam.openai.api.assistant.AssistantId
import com.aallam.openai.api.assistant.AssistantRequest
import com.aallam.openai.api.core.DeleteResponse
import com.aallam.openai.api.core.ListResponse
import com.aallam.openai.api.core.SortOrder
import com.aallam.openai.api.exception.OpenAIAPIException
import com.aallam.openai.api.file.FileId
import com.aallam.openai.client.Assistants
import com.aallam.openai.client.internal.extension.beta
import com.aallam.openai.client.internal.http.HttpRequester
import com.aallam.openai.client.internal.http.perform
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

internal class AssistantsApi(val requester: HttpRequester) : Assistants {
    @BetaOpenAI
    override suspend fun assistant(request: AssistantRequest): Assistant {
        return requester.perform {
            it.post {
                url(path = ApiPath.Assistants)
                setBody(request)
                contentType(ContentType.Application.Json)
                beta("assistants", 1)
            }.body()
        }
    }

    @BetaOpenAI
    override suspend fun assistant(id: AssistantId): Assistant? {
        try {
            return requester.perform<HttpResponse> {
                it.get {
                    url(path = "${ApiPath.Assistants}/${id.id}")
                    beta("assistants", 1)
                }
            }.body()
        } catch (e: OpenAIAPIException) {
            if (e.statusCode == HttpStatusCode.NotFound.value) return null
            throw e
        }
    }

    @BetaOpenAI
    override suspend fun assistant(id: AssistantId, request: AssistantRequest): Assistant {
        return requester.perform {
            it.post {
                url(path = "${ApiPath.Assistants}/${id.id}")
                setBody(request)
                contentType(ContentType.Application.Json)
                beta("assistants", 1)
            }.body()
        }
    }

    @BetaOpenAI
    override suspend fun delete(id: AssistantId): Boolean {
        val response = requester.perform<HttpResponse> {
            it.delete {
                url(path = "${ApiPath.Assistants}/${id.id}")
                beta("assistants", 1)
            }
        }
        return when (response.status) {
            HttpStatusCode.NotFound -> false
            else -> response.body<DeleteResponse>().deleted
        }
    }

    @BetaOpenAI
    override suspend fun delete(assistantId: AssistantId, fileId: FileId): Boolean {
        val response = requester.perform<HttpResponse> {
            it.delete {
                url(path = "${ApiPath.Assistants}/${assistantId.id}/files/${fileId.id}")
                beta("assistants", 1)
            }
        }
        return when (response.status) {
            HttpStatusCode.NotFound -> false
            else -> response.body<DeleteResponse>().deleted
        }
    }

    @BetaOpenAI
    override suspend fun assistants(limit: Int?, order: SortOrder?, after: String?, before: String?): List<Assistant> {
        return requester.perform<ListResponse<Assistant>> { client ->
            client.get {
                url {
                    path(ApiPath.Assistants)
                    limit?.let { parameter("limit", it) }
                    order?.let { parameter("order", it.order) }
                    after?.let { parameter("after", it) }
                    before?.let { parameter("before", it) }
                }
                beta("assistants", 1)
            }.body()
        }
    }

    @BetaOpenAI
    override suspend fun create(assistantId: AssistantId, fileId: FileId): AssistantFile {
        val request = buildJsonObject { put("file", fileId.id) }
        return requester.perform {
            it.post {
                url(path = "${ApiPath.Assistants}/${assistantId.id}")
                setBody(request)
                contentType(ContentType.Application.Json)
                beta("assistants", 1)
            }.body()
        }
    }

    @BetaOpenAI
    override suspend fun file(assistantId: AssistantId, fileId: FileId): AssistantFile {
        return requester.perform {
            it.get {
                url(path = "${ApiPath.Assistants}/${assistantId.id}/files/${fileId.id}")
                beta("assistants", 1)
            }
        }
    }

    @BetaOpenAI
    override suspend fun files(
        id: AssistantId,
        limit: Int?,
        order: SortOrder?,
        after: String?,
        before: String?
    ): List<AssistantFile> {
        return requester.perform<ListResponse<AssistantFile>> { client ->
            client.get {
                url {
                    path("${ApiPath.Assistants}/${id.id}/files")
                    limit?.let { parameter("limit", it) }
                    order?.let { parameter("order", it.order) }
                    after?.let { parameter("after", it) }
                    before?.let { parameter("before", it) }
                }
                beta("assistants", 1)
            }.body()
        }
    }
}
