# Versioning the JSON

Kondor-JSON supports versioned converters to help you manage **evolving data structures** and maintain backward compatibility with older JSON data formats.

## 🧭 What is VersionedConverter?

`VersionedConverter` is a base class you can extend to create version-aware converters. It allows mapping different versions of a data class from multiple JSON structures to a single unified Kotlin representation.

To use it, you must override:

```kotlin
abstract fun converterForVersion(version: String): ObjectNodeConverter<T>?
abstract val outputVersion: String?
```

These define how to select the appropriate deserializer based on the version string, and optionally how to mark which version to emit when serializing.

By default, the version is written into the JSON as a field called `"@version"`. You can customize this by overriding:

```kotlin
open val versionFieldName: String = "@version"
```

You can also support multiple version strings mapping to the same converter to support legacy aliases or smooth upgrades.

---

## 🎯 Use Case

Let's say your model evolved like this:

### Version 1:

```kotlin
data class PersonV1(val name: String)
```

### Version 2:

```kotlin
data class Person(val fullName: String, val age: Int)
```

You want to support reading `PersonV1` JSON while using `Person` in your codebase.

---

## 🧱 Setup

```kotlin
data class Person(val fullName: String, val age: Int)

object PersonConverterV1 : JConverterObject<Person>() {
    private val name by str()

    override fun JsonNodeObject.deserializeOrThrow(): Person =
        Person(
            fullName = +name,
            age = 0 // default age
        )
}

object PersonConverterV2 : JConverterObject<Person>() {
    private val fullName by str()
    private val age by int()

    override fun JsonNodeObject.deserializeOrThrow(): Person =
        Person(
            fullName = +fullName,
            age = +age
        )
}

object VersionedPerson : VersionedConverter<Person>() {
    override fun converterForVersion(version: String) = when (version) {
        "1" -> PersonConverterV1
        "2" -> PersonConverterV2
        else -> null
    }

    override val outputVersion = "2"
}
```

### 🧪 Example JSON Output

When serializing a `Person`, Kondor will automatically include the version:

```json
{
  "@version": "2",
  "fullName": "Alice",
  "age": 30
}
```

### Reading Legacy JSON

```kotlin
val legacyJson = JsonParser.parse('{"@version":"1", "name":"Alice"}')
val person = VersionedPerson.fromJson(legacyJson)
```

This results in:

```kotlin
Person(fullName = "Alice", age = 0)
```

---

## 🚦 VersionMapConverter

If you prefer a simpler way to manage versioned converters, Kondor provides a convenient helper class: `VersionMapConverter`.

Instead of overriding methods, you just pass a version-to-converter map to the constructor. The first entry in the map is used as the output version when serializing.

### 🔧 Setup

```kotlin
object VersionedPerson2 : VersionMapConverter<Person>(
    versionConverters = mapOf(
        "2" to PersonConverterV2,
        "1" to PersonConverterV1
    )
)
```

This achieves the same behavior as the manual implementation using `VersionedConverter`, but with less boilerplate.

- When reading JSON, the `@version` field (by default) determines which converter is used.
- When writing JSON, the first entry (`"2"` here) is assumed to be the current version.

### 🧪 Output JSON

```json
{
  "@version": "2",
  "fullName": "Alice",
  "age": 30
}
```

---

## 🕵️ Handling Unversioned JSON

Sometimes you only start versioning after data has already been serialized without any version markers. In this case, Kondor allows you to define a fallback strategy using `unversionedConverters`.

These converters are tried **in order** until one successfully parses the input JSON.

### Using with `VersionMapConverter`

```kotlin
object VersionedPerson3 : VersionMapConverter<Person>(
    versionConverters = mapOf(
        "2" to PersonConverterV2,
        "1" to PersonConverterV1
    ),
    override val unversionedConverters: List<ObjectNodeConverter<Person>> = 
      listOf(ReallyOldPersonConverter, EvenOlderPersonConverter)
)
```

### Using with `VersionedConverter`

```kotlin
object VersionedPerson4 : VersionedConverter<Person>() {
    override fun converterForVersion(version: String) = when (version) {
        "1" -> PersonConverterV1
        "2" -> PersonConverterV2
        else -> null
    }

    override val outputVersion = "2"

    override val unversionedConverters: List<ObjectNodeConverter<Person>> = 
      listOf(ReallyOldPersonConverter, EvenOlderPersonConverter)
}
```

This is useful when handling legacy data that doesn’t include version markers but still needs to be parsed reliably.

### 🧪 Using Only Unversioned Converters

You can also use `VersionMapConverter` without providing any versioned converter mappings at all — just a list of `unversionedConverters`. This is useful when you're dealing with legacy JSON that never included versioning information.

```kotlin
object LegacyPersonSupport : VersionMapConverter<Person>(
    unversionedConverters = listOf(
        ReallyOldPersonConverter,
        EvenOlderPersonConverter
    )
)
```

In this setup:

- There is **no need** for a `@version` field in the JSON.
- Kondor will try each converter in order until one successfully parses the object.
- This allows you to incrementally introduce versioning without needing to rewrite all your old data.

---

## 💡 Tip

Versioning strategy can be:

- File-based (metadata stored separately)
- Embedded in the JSON (e.g., a `@version` field, which is the default in Kondor)

You can also support multiple version strings mapping to the same converter to allow for legacy compatibility or aliases like `"latest"` or `"2.0"`.

---

## 🔗 Related

- [Getting Started](getting-started.md)
- [JStringRepresentable](jstring-representable.md)

