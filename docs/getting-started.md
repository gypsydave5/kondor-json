# Getting Started

Welcome to **Kondor-JSON**! This guide will help you get up and running quickly with real examples based on the actual API.

## 🔧 Installation

Add Kondor-JSON to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.ubertob.kondor:kondor-json:<latest-version>")
}
```

> 📦 Check [Maven Central](https://search.maven.org/search?q=kondor-json) for the latest version.

---

## 🧠 Core Concepts

Kondor represents JSON through a strongly-typed model:

- `JAny` — the root type for all JSON values
- `JString`, `JInt`, `JBool`, etc. — primitive value types
- `JObj`, `JArray` — container types
- `JConverter<T>` — bridges Kotlin types and JSON representations

---

## ✅ Real-World Example

```kotlin
import com.ubertob.kondor.json.*
import com.ubertob.kondor.json.jvalue.*

data class Person(val name: String, val age: Int)

object PersonConverter : JConverterObject<Person>() {
    private val name by str()
    private val age by int()

    override fun JsonNodeObject.deserializeOrThrow(): Person =
        Person(
            name = +name,
            age = +age
        )

    override fun Person.serialize(): JsonNodeObject =
        jsonObj {
            name of it.name
            age of it.age
        }
}

fun main() {
    val person = Person("Alice", 30)
    val json = PersonConverter.toJsonStr(person)
    println(json)
    println(PersonConverter.fromJsonStr(json))
}
```

### Output:
```json
{"name":"Alice","age":30}
Person(name=Alice, age=30)
```

---

## ⏭️ Next Steps

- Learn about [VersionedConverter](versioned-converter.md) for schema evolution
- Browse [API Reference](api-reference.md)
- See how to use [JStringRepresentable](jstring-representable.md)
