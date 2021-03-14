package com.ubertob.kondor.json


import JsonLexer
import com.ubertob.kondor.outcome.*


data class JsonError(val path: NodePath, val reason: String) : OutcomeError {
    override val msg = "error on <${path.getPath()}> $reason"

    override fun toString(): String = msg
}

typealias JsonOutcome<T> = Outcome<JsonError, T>

typealias JConverter<T> = JsonAdjunction<T, *>


interface JsonAdjunction<T, JN : JsonNode> {

    val nodeType: NodeKind<JN>

    @Suppress("UNCHECKED_CAST") //but we are confident it's safe
    private fun safeCast(node: JsonNode): JsonOutcome<JN> =
        if (node.nodeKind() == nodeType)
            (node as JN).asSuccess()
        else
            JsonError(node.path, "expected a ${nodeType.desc} but found ${node.nodeKind().desc}").asFailure()

    fun fromJsonNodeBase(node: JsonNode): JsonOutcome<T> = safeCast(node).bind(::fromJsonNode)
    fun fromJsonNode(node: JN): JsonOutcome<T>
    fun toJsonNode(value: T, path: NodePath): JN

    private fun TokensStream.parseFromRoot(): JsonOutcome<JN> =
        nodeType.parse(this, NodeRoot)

    fun toJson(value: T): String = toJsonNode(value, NodeRoot).render()

    fun fromJson(jsonString: String): JsonOutcome<T> =
        JsonLexer.tokenize(jsonString).run {
            parseFromRoot()
                .bind { fromJsonNode(it) }
                .bind {
                    if (hasNext())
                        parsingFailure("EOF", next(), position(), NodeRoot, "json continue after end")
                    else
                        it.asSuccess()
                }
        }

}






