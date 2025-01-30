package com.ubertob.kondor.json

import com.ubertob.kondor.json.jsonnode.JsonNodeObject
import com.ubertob.kondor.json.schema.enumSchema
import com.ubertob.kondor.outcome.asSuccess
import java.math.BigDecimal
import java.math.BigInteger
import java.util.*
import kotlin.reflect.KClass


interface StringWrapper {
    val raw: String
}

data class JStringWrapper<T : StringWrapper>(override val cons: (String) -> T) : JStringRepresentable<T>() {
    override val render: (T) -> String = { it.raw }
}

object JUUID : JStringRepresentable<UUID>() {
    override val cons = UUID::fromString
    override val render = UUID::toString
}

object JBigDecimal : JNumRepresentable<BigDecimal>() {
    override val cons: (Number) -> BigDecimal = { BigDecimal(it.toString()) }
    override val render: (BigDecimal) -> Number = { it }
    override fun parser(value: String): JsonOutcome<BigDecimal> = BigDecimal(value).asSuccess()

}

object JBigInteger : JNumRepresentable<BigInteger>() {
    override val cons: (Number) -> BigInteger = { BigInteger(it.toString()) }
    override val render: (BigInteger) -> Number = BigInteger::toBigDecimal
    override fun parser(value: String): JsonOutcome<BigInteger> = BigInteger(value).asSuccess()

}


object JCurrency : JStringRepresentable<Currency>() {
    override val cons: (String) -> Currency = Currency::getInstance
    override val render: (Currency) -> String = Currency::getCurrencyCode
}


data class JEnum<E : Enum<E>>(override val cons: (String) -> E, val values: List<E> = emptyList()) :
    JStringRepresentable<E>() {
    override val render: (E) -> String = { it.name } //see enumValueOf() and enumValues()
    override fun schema(): JsonNodeObject = enumSchema(values)
}

data class JEnumClass<E : Enum<E>>(val clazz: KClass<E>) : JStringRepresentable<E>() {
    private val valuesMap: Map<String, E> by lazy { clazz.java.enumConstants.associateBy { it.name } }
    override val cons: (String) -> E = { name -> valuesMap[name] ?: error("not found $name among ${valuesMap.keys}") }
    override val render: (E) -> String = { it.name }
    override fun schema(): JsonNodeObject = enumSchema(valuesMap.values.toList())
}

//for serializing Kotlin object and other single instance types
data class JInstance<T : Any>(val singleton: T) : JAny<T>() {
    override fun JsonNodeObject.deserializeOrThrow() = singleton
}

