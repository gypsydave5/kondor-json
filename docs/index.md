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
    val json = PersonConverter.toJson(person)
    val samePerson = PersonConverter.fromJson(json).orThrow()
}
```

## 📌 What's Next?

Explore more of Kondor’s features:

- 💻 [Getting Started](getting-started.md) – installing Kondor
- 🛠 [Using JAny](jany.md) - the general converter for objects
- 🔤 [Adding Fields](short-field-functions.md) - for adding new fields to an object converter
