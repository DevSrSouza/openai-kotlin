package com.aallam.openai.client.extension.internal

import com.aallam.openai.api.chat.*
import com.aallam.openai.api.chat.internal.ToolType

/**
 * A class to help assemble chat messages from chat chunks.
 */
internal class ChatMessageAssembler {
    private val chatFuncName = StringBuilder()
    private val chatFuncArgs = StringBuilder()
    private val chatContent = StringBuilder()
    private var chatRole: ChatRole? = null
    private val toolCallsAssemblers = mutableMapOf<Int, ToolCallAssembler>()

    /**
     * Merges a chat chunk into the chat message being assembled.
     */
    fun merge(chunk: ChatChunk): ChatMessageAssembler {
        chunk.delta.run {
            role?.let { chatRole = it }
            content?.let { chatContent.append(it) }
            functionCall?.let { call ->
                call.nameOrNull?.let { chatFuncName.append(it) }
                call.argumentsOrNull?.let { chatFuncArgs.append(it) }
            }
            toolCalls?.onEach { toolCall ->
                val assembler = toolCallsAssemblers.getOrPut(toolCall.index) { ToolCallAssembler() }
                assembler.merge(toolCall)
            }
        }
        return this
    }

    /**
     * Builds and returns the assembled chat message.
     */
    fun build(): ChatMessage = chatMessage {
        this.role = chatRole
        this.content = chatContent.toString()
        if (chatFuncName.isNotEmpty() || chatFuncArgs.isNotEmpty()) {
            this.functionCall = FunctionCall(chatFuncName.toString(), chatFuncArgs.toString())
            this.name = chatFuncName.toString()
        }
        if (toolCallsAssemblers.isNotEmpty()) {
            this.toolCalls = toolCallsAssemblers.map { (_, value) -> value.build() }.toList()
        }
    }
}

internal class ToolCallAssembler {
    private var toolIndex: Int? = null
    private var toolId: ToolId? = null
    private var toolType: ToolType? = null
    private var funcName: String? = null
    private val funcArgs = StringBuilder()

    fun merge(toolCall: ToolCall): ToolCallAssembler {
        toolCall.indexOrNull?.let { toolIndex = it }
        toolCall.idOrNull?.let { toolId = it }
        toolCall.typeOrNull?.let { toolType = it }
        toolCall.functionOrNull?.let { call ->
            call.nameOrNull?.let { funcName = it }
            call.argumentsOrNull?.let { funcArgs.append(it) }
        }
        return this
    }

    /**
     * Builds and returns the assembled chat message.
     */
    fun build(): ToolCall = toolCall {
        this.index = toolIndex
        this.id = toolId
        this.type = toolType
        if (funcName?.isNotEmpty() == true || funcArgs.isNotEmpty()) {
            this.function = FunctionCall(funcName, funcArgs.toString())
        }
    }
}
