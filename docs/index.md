# Kondor-JSON

Welcome to **Kondor-JSON**, a Kotlin-first JSON serialization library that emphasizes type-safety, composability, and clarity.

## 🔍 What is Kondor-JSON?

Kondor-JSON allows you to define type-safe, bidirectional mappings between your Kotlin data classes and JSON documents using a flexible DSL. It avoids reflection and favors a functional style to keep your data transformations explicit and testable.

## 🚀 Features
- Type-safe JSON serialization/deserialization
- Fully customizable converters
- Versioned converters for backward compatibility
- String representable support for enums and sealed classes
- No reflection, works in Kotlin Multiplatform

## 🛠️ Example

```kotlin
import com.ubertob.kondor.json.*
import com.ubertob.kondor.json.jvalue.*

data class Person(val name: String, val age: Int)

object PersonConverter : JAny<Person>() {
    private val name by str(Person::name)
    private val age by int(Person::age)

    override fun JsonNodeObject.deserializeOrThrow(): Person =
        Person(
            name = +name,
            age = +age
        )
}

fun main() {
    val person = Person("Alice", 30)
    val json = PersonConverter.toJsonStr(person)
    PersonConverter.fromJsonStr(json)
}
```

## 📌 What's Next?

Explore more of Kondor’s features:

- 💻 [Converters Overview](getting-started.md) – an overview understanding how to work with Kondor converters
- 🔁 [Versioning the JSON](versioned-converter.md) – for evolving schemas
- 🔤 [Tiny Types (Value Objects)](tiny-types.md) – for handling object you want to represent as a string or number
- 🔤 [Enums and Sealed Classes](enums-and-sealed.md) – for working with enums and sealed classes
- 🛠 [Field Functions](short-field-functions.md) – helper functions for defining fields in Kondor converters
