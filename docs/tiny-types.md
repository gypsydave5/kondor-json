
# Tiny Types (Value Objects)

In many Kotlin applications, we use *tiny types* — sometimes called *value objects* — to wrap primitive values with domain-specific meaning. For example, we might wrap a `String` in an `Email` class, or a `Double` in a `Price` class.

This is a common technique for adding clarity and safety to your code, and is described well in [this article](https://darrenhobbs.com/2007/04/11/tiny-types/) by Darren Hobbs.

Kotlin has first-class support for these kinds of types using [inline value classes](https://kotlinlang.org/docs/inline-classes.html), which allow you to wrap a value while avoiding runtime overhead. Kondor builds on this concept by providing utilities to serialize and deserialize these types easily, avoiding the "object" overhead in the JSON.

Kondor supports these types with minimal boilerplate. In this section, we’ll look at a couple of strategies for working with tiny types.

---

## 🧱 The Default Approach

If you don't define a custom converter for your tiny type, Kondor will serialize it as a regular object. For example:

```kotlin
class Email(val raw: String)

object JEmail : JAny<Email>() {
    val raw by str(Email::raw)

    override fun JsonNodeObject.deserializeOrThrow() =
        Email(raw = +raw)
}

data class User(val name: String, val email: Email)

object JUser : JAny<User>() {
    val name by str(User::name)
    val email by str(JEmail, User::email)

    override fun JsonNodeObject.deserializeOrThrow() =
        User(
            name = +name,
            email = +email
        )
}
```

### Resulting JSON

```json
{
  "name": "Alice",
  "email": {
    "raw": "alice@example.com"
  }
}
```

This works just fine, but adds unnecessary structure to the JSON — the `email` is just a string, so it makes sense to avoid wrapping it as an object. That’s where `JStringRepresentable` or factory-style helpers come in.

---

## 🔧 Using `JStringRepresentable`

If your tiny type wraps a `String` but doesn't follow the wrapper class conventions, or if you need custom conversion logic, you can create a converter by subclassing `JStringRepresentable`.

> 🔄 You can easily adapt this approach to work with other tiny type libraries such as [values4k](https://github.com/jcornaz/values). As long as you can define a function to go from a string to your value object (e.g., from JSON) and back (e.g., to JSON), you can implement `JStringRepresentable` for any type.

`JStringRepresentable` is used to create a bidirectional mapping between a JSON string and your custom type. This is done by implementing two functions:

- `cons` — a function that takes a `String` and returns an instance of your type.
- `render` — a function that takes an instance of your type and returns a `String`.

Together, these provide the two-way transformation between the JSON representation and the Kotlin object.

### Example

```kotlin
class Email(val raw: String)

object JEmail : JStringRepresentable<Email>() {
    override val cons: (String) -> Email = ::Email
    override val render: (Email) -> String = { it.raw }
}

// Use it in a data class
data class User(val name: String, val email: Email)

object JUser : JAny<User>() {
    val name by str(User::name)
    val email by str(JEmail, User::email)

    override fun JsonNodeObject.deserializeOrThrow() =
        User(
            name = +name,
            email = +email
        )
}
```

### Resulting JSON

```json
{
  "name": "Alice",
  "email": "alice@example.com"
}
```

This approach gives you full control over how your tiny type is converted to and from strings, which can be helpful if you need to support legacy formats or add validation.

---

## 🔄 Writing Your Own Converter Factories

If you're using a consistent way to define your tiny types, you can create reusable factories to produce converters. This helps reduce duplication across many tiny types.

Here’s how you could define a helper converter for tiny types from [values4k](https://github.com/fork-handles/forkhandles/tree/trunk/values4k):

```kotlin
import dev.forkhandles.values.StringValue

@JvmInline
value class Email(override val value: String) : StringValue<Email>

class JStringValue<T : StringValue<T>>(
    private val konstructor: (String) -> T
) : JStringRepresentable<T>() {
    override val cons: (String) -> T = konstructor
    override val render: (T) -> String = { it.value }
}

object JEmail : JStringValue<Email>(::Email)

data class User(val name: String, val email: Email)

object JUser : JAny<User>() {
    val name by str(User::name)
    val email by stru(JEmail, User::email)

    override fun JsonNodeObject.deserializeOrThrow() =
        User(
            name = +name,
            email = +email
        )
}
```

### Resulting JSON

```json
{
  "name": "Alice",
  "email": "alice@example.com"
}
```

This lets you easily reuse a single converter factory for any of your value types that extend `StringValue`, keeping your JSON clean and your code DRY.

---

## 📚 Other `J*Representable` Classes

In addition to `JStringRepresentable`, Kondor provides similar helpers for other primitive types. These include:

- `JIntRepresentable<T>` — for types that wrap `Int`
- `JLongRepresentable<T>` — for types that wrap `Long`
- `JDoubleRepresentable<T>` — for types that wrap `Double`
- `JBoolRepresentable<T>` — for types that wrap `Boolean`
- `JFloatRepresentable<T>` — for types that wrap `Float`
- `JBigIntRepresentable<T>` — for types that wrap `BigInteger`

Each of these classes lets you define converters for tiny types by providing `cons` and `render` functions, just like `JStringRepresentable`.

These are especially useful when working with domain primitives like `UserId`, `Price`, `Flag`, etc., that wrap core types while preserving type safety and clarity in your domain model.

## 📌 What's Next?

Explore more of Kondor’s features:

- 💻 [Converters Overview](getting-started.md) – an overview understanding how to work with Kondor converters
- 🔁 [Versioning the JSON](versioned-converter.md) – for evolving schemas
- 🔤 [Tiny Types (Value Objects)](tiny-types.md) – for handling object you want to represent as a string or number
- 🔤 [Enums and Sealed Classes](enums-and-sealed.md) – for working with enums and sealed classes
- 🛠 [Field Functions](short-field-functions.md) – helper functions for defining fields in Kondor converters
- 🧳 [Sealed Classes and Polymorphic JSON](sealed-classes.md) – handling polymorphic JSON with sealed classes
