package com.ubertob.kondor.json

import com.ubertob.kondor.json.JsonStyle.Companion.appendText
import com.ubertob.kondor.json.jsonnode.*
import com.ubertob.kondor.json.schema.sealedSchema

abstract class PolymorphicConverter<T : Any> : ObjectNodeConverterBase<T>() {
    abstract fun extractTypeName(obj: T): String
    abstract val subConverters: Map<String, ObjectNodeConverterBase<out T>>

    @Suppress("UNCHECKED_CAST")
    fun findSubTypeConverter(typeName: String): ObjectNodeConverterBase<T>? =
        subConverters[typeName] as? ObjectNodeConverterBase<T>

}

abstract class JSealed<T : Any> : PolymorphicConverter<T>() {

    open val discriminatorFieldName: String = "_type"

    open val defaultConverter: ObjectNodeConverterBase<out T>? = null

    private fun discriminatorFieldNode(obj: T, path: NodePath) =
        JsonNodeString(extractTypeName(obj), NodePathSegment(discriminatorFieldName, path))

    override fun JsonNodeObject.deserializeOrThrow(): T? {

        val discriminatorNode =
            _fieldMap[discriminatorFieldName] ?: defaultConverter?.let { return it.fromJsonNode(this).orThrow() }
            ?: error("expected discriminator field \"$discriminatorFieldName\" not found")

        val typeName = JString.fromJsonNodeBase(discriminatorNode).orThrow()
        val converter = subConverters[typeName] ?: error("subtype not known $typeName")
        return converter.fromJsonNode(this).orThrow()
    }

    override fun convertFields(valueObject: T, path: NodePath): Map<String, JsonNode> =
        extractTypeName(valueObject).let { typeName ->
            findSubTypeConverter(typeName)?.convertFields(valueObject, path)
                ?.also { (it as MutableMap)[discriminatorFieldName] = discriminatorFieldNode(valueObject, path) }
                ?: error("subtype not known $typeName")
        }


    override fun fieldAppenders(valueObject: T): Map<String, PropertyAppender> =
        extractTypeName(valueObject).let { typeName ->
            mutableMapOf(appendTypeName(discriminatorFieldName, typeName)).apply {
                putAll(
                    converterFromTypename(typeName, valueObject)
                        ?: error("subtype not known $typeName")
                )
            }
        }

    private fun converterFromTypename(typeName: String, valueObject: T) =
        findSubTypeConverter(typeName) ?.fieldAppenders(valueObject)

            private fun appendTypeName(discriminatorFieldName: String, typeName: String): Pair<String, PropertyAppender> =
        discriminatorFieldName to { app: StrAppendable, style: JsonStyle, off: Int ->
            app.appendText(discriminatorFieldName)
                .append(style.valueSeparator)
                .appendText(typeName)
            true
        }

    override fun schema() = sealedSchema(discriminatorFieldName, subConverters)

}