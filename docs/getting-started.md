# Getting Started

Welcome to **Kondor-JSON**! This guide will help you get up and running quickly, and explain the **core concepts** that make Kondor-JSON unique.

---

## 🔧 Installation

Add Kondor-JSON to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.ubertob.kondor:kondor-json:<latest-version>")
}
```

> 📦 Check [Maven Central](https://search.maven.org/search?q=kondor-json) for the latest version.

---

## 🎯 Core Concept

Unlike many serialization libraries, **Kondor-JSON does not use intermediate DTOs**. Instead, you define a *single, bidirectional converter* that maps your Kotlin type directly to and from JSON using `JConverter`.

Kondor internally represents JSON objects using a type called `JsonNodeObject`. This is a tree-like structure that mirrors JSON syntax.

Most of the time, **you don’t need to interact with ****\`\`**** directly**—the DSL handles it for you via field bindings and converters.

Each converter is responsible for:

- Parsing a `JsonNodeObject` into your type (`deserializeOrThrow`)
- Serializing your type into a `JsonNodeObject` (default via DSL delegates, or override if needed)

---

## ✨ Example: Using `JAny` with the Kondor DSL

Let’s define a `Person` class and create a converter by extending `JAny<Person>`. We'll use Kondor’s DSL-style field delegates to map each property, and take advantage of the `+` bind operator for clean deserialization.

```kotlin
import com.ubertob.kondor.json.*
import com.ubertob.kondor.json.jvalue.*

data class Person(val name: String, val age: Int)

object PersonConverter : JAny<Person>() {
    val name by str(Person::name)
    val age by num(Person::age)

    override fun JsonNodeObject.deserializeOrThrow(): Person =
        Person(
            name = +name,
            age = +age
        )
}
```

This pattern offers a clean and expressive way to map fields.

📝 **Convention Note**: While we’ve named our converter `PersonConverter` here, it’s common in Kondor projects to use a shorter name like `JPerson`. This saves typing without sacrificing clarity—but you’re free to choose whatever naming style fits your project best.

---

## 🧪 Usage

```kotlin
fun main() {
    val person = Person("Alice", 30)
    val json = PersonConverter.toJson(person)
    println(json) // {"name":"Alice","age":30}

    val parsed = PersonConverter.fromJson(json)
    println(parsed) // Person(name=Alice, age=30)
}
```

---

## 🧠 What's Going On?

### 📦 What is JAny?

`JAny<T>` is the top-level abstraction in the Kondor DSL for converting objects. It is the class you will use most often when working with Kondor.

You define a subclass of `JAny` for each type you want to serialize, and use the DSL to declare how its properties map to and from JSON.

This allows you to skip intermediate DTOs and instead declare the full serialization contract in one place.

### 🔑 Defining Fields

We start by defining fields using the DSL delegates like `str` and `num`. These fields form the shape of the JSON representation.

The **names you give these fields in your converter directly become the property names** in the serialized JSON object.

For example, in the converter we define `val name by str(Person::name)`, so the resulting JSON will include a property named `"name"`. If you changed this to `val fullName by str(Person::name)`, the output JSON would have `"fullName"` instead.

### 🧩 How Fields Work

This section explains how basic field mapping works using the DSL.

The functions `str` and `num` are part of Kondor’s DSL for defining fields. They each take arguments that describe how to serialize and deserialize the values:

- `str(Person::name)` means "this field maps to a JSON string, and we get its value from the `name` property of the `Person` object."
- `num(Person::age)` similarly maps the `age` field to a JSON number, pulling it from the `age` property of `Person`.

In many cases, these mappings can be inferred automatically—for instance, Kotlin `String` to JSON string is a direct mapping. But we still need to tell Kondor *which* property of the object we are talking about—hence the use of property references like `Person::name`.

We'll cover more complex examples later, including how to handle custom types, nullable fields, and schema evolution.

### ➕ The Bind Operator

We use the `+` operator (also known as the *bind operator*) to extract values from the `JsonNodeObject`.

For example:

```kotlin
name = +name
```

This line binds the value of the JSON `"name"` field (as defined by the delegate) into the `name` property of the `Person` object.

## 📌 What's Next?

Once you're comfortable with this approach, explore more advanced features:


- 💻 [Converters Overview](getting-started.md) – an overview understanding how to work with Kondor converters
- 🔁 [Versioning the JSON](versioned-converter.md) – for evolving schemas
- 🔤 [Tiny Types (Value Objects)](tiny-types.md) – for handling object you want to represent as a string or number
- 🔤 [Enums and Sealed Classes](enums-and-sealed.md) – for working with enums and sealed classes
- 🛠 [Field Functions](short-field-functions.md) – helper functions for defining fields in Kondor converters
- 🧳 [Sealed Classes and Polymorphic JSON](sealed-classes.md) – handling polymorphic JSON with sealed classes


---

## 📘 Key Takeaways

- 🚫 Kondor doesn't rely on intermediate DTOs. Instead, you define mapping functions that convert to and from JSON directly.
- 🤖 Much of the mapping logic is inferred automatically by the Kondor DSL.
- 📦 If you're unsure what kind of converter to use, `JAny` is likely the one you need—it's the most commonly used abstraction in the DSL.

