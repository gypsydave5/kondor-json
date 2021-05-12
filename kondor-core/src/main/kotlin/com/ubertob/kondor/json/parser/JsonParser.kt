package com.ubertob.kondor.json.parser

import com.ubertob.kondor.json.JsonError
import com.ubertob.kondor.json.JsonOutcome
import com.ubertob.kondor.json.jsonnode.*
import com.ubertob.kondor.outcome.*
import com.ubertob.kondor.outcome.Outcome.Companion.tryOrFail
import java.math.BigDecimal

private inline fun <T> tryParse(
    expected: String,
    actual: KondorToken,
    position: Int,
    path: NodePath,
    f: () -> T
): Outcome<JsonError, T> =
    tryOrFail(f)
        .transformFailure {
            when (it.throwable) {
                is NumberFormatException ->
                    parsingError(expected, "$actual", position, path, it.msg)
                else ->
                    parsingError(expected, "${it.msg} after $actual", position, path, "Invalid Json")
            }
        }

data class TokensPath(val tokens: TokensStream, val path: NodePath)

fun <T> tryParseBind(
    expected: String,
    tokens: TokensStream,
    path: NodePath,
    f: TokensPath.() -> Outcome<JsonError, T>
): Outcome<JsonError, T> =
    try {
        f(TokensPath(tokens, path))
    } catch (t: NumberFormatException) {
        parsingError(expected, tokens.last().toString(), tokens.position(), path, t.message.orEmpty()).asFailure()
    } catch (t: Throwable) {
        parsingError(
            expected,
            "${t.message.orEmpty()} after ${tokens.last()}",
            tokens.position(),
            path,
            "Invalid Json"
        ).asFailure()
    }


fun parsingError(expected: String, actual: String, position: Int, path: NodePath, details: String) = JsonError(
    path, "at position $position: expected $expected but found '$actual' - $details"
)

fun parsingFailure(expected: String, actual: String, position: Int, path: NodePath, details: String) =
    parsingError(expected, actual, position, path, details).asFailure()

fun parsingFailure(expected: String, actual: KondorToken, position: Int, path: NodePath, details: String) =
    parsingError(expected, actual.toString(), position, path, details).asFailure()


//todo delete these and just use inner boolean in NodeKind.
fun parseJsonNodeBoolean(
    tokens: TokensStream,
    path: NodePath
): JsonOutcome<JsonNodeBoolean> =
    tryParseBind(
        "a Boolean", tokens, path,
        TokensPath::boolean
    )

fun parseJsonNodeNum(
    tokens: TokensStream,
    path: NodePath
): Outcome<JsonError, JsonNodeNumber> =
    tryParseBind(
        "a Number", tokens, path,
        TokensPath::number
    )

fun parseJsonNodeNull(
    tokens: TokensStream,
    path: NodePath
): Outcome<JsonError, JsonNodeNull> =
    tryParseBind(
        "a Null", tokens, path,
        TokensPath::explicitNull
    )

typealias JsonParser<T> = (TokensPath) -> JsonOutcome<T>

fun parseJsonNodeString(
    tokens: TokensStream,
    path: NodePath
): Outcome<JsonError, JsonNodeString> =
    tryParseBind(
        "a String", tokens, path,
        OpeningQuotes `(` TokensPath::string `)` ClosingQuotes
    )

private fun TokensPath.boolean(): JsonOutcome<JsonNodeBoolean> =
    when (val token = tokens.next()) {
        Value("true") -> true.asSuccess()
        Value("false") -> false.asSuccess()
        else -> parsingFailure("a Boolean", token, tokens.position(), path, "valid values: false, true")
    }.transform { JsonNodeBoolean(it, path) }


private fun TokensPath.number(): JsonOutcome<JsonNodeNumber> =
    when (val token = tokens.next()) {
        is Value -> BigDecimal(token.text).asSuccess()
        else -> parsingFailure("a Number", token, tokens.position(), path, "not a valid number")
    }.transform { JsonNodeNumber(it, path) }


private fun TokensPath.explicitNull(): JsonOutcome<JsonNodeNull> =
    when (val token = tokens.next()) {
        Value("null") -> Unit.asSuccess()
        else -> parsingFailure("a Null", token, tokens.position(), path, "valid values: null")
    }.transform { JsonNodeNull(path) }


//todo extract the optionality...
private fun TokensPath.string(): JsonOutcome<JsonNodeString> =
    when (val token = tokens.peek()) {
        is Value -> token.text.asSuccess().also { tokens.next() }
        else -> "".asSuccess()
    }.transform { JsonNodeString(it, path) }


//todo refactor out failure in ( and )
infix fun <T> KondorToken.`(`(content: JsonParser<T>): JsonParser<T> = { tokensPath ->
    val token = tokensPath.tokens.next()
    if (token != this)
        parsingFailure(
            this.toString(),
            token,
            tokensPath.tokens.position(),
            tokensPath.path,
            "missing ${this}"
        )
    else
        content(tokensPath)
}


infix fun <T> JsonParser<T>.`)`(expected: KondorToken): JsonParser<T> = { tokensPath ->
    this(tokensPath).bind {
        val token = tokensPath.tokens.next()
        if (token != expected)
            parsingFailure(
                expected.toString(),
                token,
                tokensPath.tokens.position(),
                tokensPath.path,
                "missing ${expected}"
            )
        else
            it.asSuccess()
    }
}

//---


fun parseJsonNodeArray(
    tokens: TokensStream,
    path: NodePath
): JsonOutcome<JsonNodeArray> =
    tryParse("an Array", tokens.peek(), tokens.position(), path) {
        val openBraket = tokens.next()
        if (openBraket != OpeningBracket)
            return parsingFailure("'['", openBraket, tokens.position(), path, "missing opening bracket")
        else {
            val nodes = mutableListOf<JsonNode>()
            var currNode = 0
            while (true) {
                parseNewNode(tokens, NodePathSegment("[${currNode++}]", path))
                    ?.onFailure { return it.asFailure() }
                    ?.also { nodes.add(it) }
                    ?: break

                if (tokens.peek() == Comma)
                    tokens.next()
                else
                    break
            }
            if (tokens.next() != ClosingBracket)
                return parsingFailure(
                    "']' or value",
                    tokens.last()!!,
                    tokens.position(),
                    path,
                    "missing closing bracket"
                )
            JsonNodeArray(nodes, path)
        }
    }

fun parseJsonNodeObject(
    tokens: TokensStream,
    path: NodePath
): Outcome<JsonError, JsonNodeObject> =  //TODO remove non local returns...
    tryParse("an Object", tokens.peek(), tokens.position(), path) {
        val openCurly = tokens.next()
        if (openCurly != OpeningCurly)
            return parsingFailure("'{'", openCurly, tokens.position(), path, "missing opening curly")
        else {

            val keys = mutableMapOf<String, JsonNode>()
            while (true) { //add a tokens foldOutcome
                if (tokens.peek() == ClosingCurly)
                    break

                val keyName = parseJsonNodeString(tokens, path).onFailure { return it.asFailure() }.text
                if (keyName in keys)
                    return parsingFailure("a unique key", keyName, tokens.position(), path, "duplicated key")

                val colon = tokens.next()
                if (colon != Colon)
                    return parsingFailure(
                        "':'",
                        colon,
                        tokens.position(),
                        path,
                        "missing colon between key and value in object"
                    )
                parseNewNode(tokens, NodePathSegment(keyName, path))
                    ?.onFailure { return it.asFailure() }
                    ?.let { keys.put(keyName, it) }

                if (tokens.peek() == Comma)
                    tokens.next()
                else
                    break

            }
            if (tokens.next() != ClosingCurly)
                return parsingFailure(
                    "'}' or key:value",
                    tokens.last()!!,
                    tokens.position(),
                    path,
                    "missing closing curly"
                )
            JsonNodeObject(keys, path)
        }
    }

fun parseNewNode(tokens: TokensStream, path: NodePath): JsonOutcome<JsonNode>? =
    when (val first = tokens.peek()) {
        Value("null") -> parseJsonNodeNull(tokens, path)
        Value("false"), Value("true") -> parseJsonNodeBoolean(tokens, path)
        is Value -> parseJsonNodeNum(tokens, path)
        OpeningQuotes -> parseJsonNodeString(tokens, path)
        OpeningBracket -> parseJsonNodeArray(tokens, path)
        OpeningCurly -> parseJsonNodeObject(tokens, path)
        ClosingQuotes, ClosingBracket, ClosingCurly, Comma, Colon -> null //no new node
    }


