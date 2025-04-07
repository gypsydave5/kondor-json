# Enums and Sealed Classes

Kondor-JSON offers seamless support for Kotlin enums, providing both a default serialization strategy and the ability to customize the behavior using `JEnumClass`.

---

## 🔤 Enums

### Default Enum Serialization

The easiest way to serialize an enum in Kondor is by using `JEnumClass`, which provides a sensible default: enum values are mapped to their `.name` strings in JSON.

```kotlin
enum class Colour { Red, Green, Blue }

object JColour : JEnumClass<Colour>(Colour::class)
```

You can then use this converter in your data classes:

```kotlin
data class Paint(val shade: Colour)

object JPaint : JAny<Paint>() {
    val shade by JColour(Paint::shade)

    override fun JsonNodeObject.deserializeOrThrow() =
        Paint(+shade)
}
```

This will serialize a `Paint` object to:

```json
{ "shade": "Red" }
```

### Customizing Enum Representation

If you'd like to represent enums using custom strings (e.g., abbreviations), you can subclass `JEnumClass` and override the `render` and `cons` methods.

```kotlin
enum class Status { Active, Inactive, Pending }

object JStatus : JEnumClass<Status>(Status::class) {
    override val render: (Status) -> String = {
        when (it) {
            Status.Active -> "A"
            Status.Inactive -> "I"
            Status.Pending -> "P"
        }
    }

    override val cons: (String) -> Status = {
        when (it) {
            "A", "Active" -> Status.Active
            "I", "Inactive" -> Status.Inactive
            "P", "Pending" -> Status.Pending
            else -> error("Unknown status: $it")
        }
    }
}
```

This allows the following round-trip:

```json
{ "status": "A" }
```

It also enables backwards compatibility with older JSON representations. For example, if earlier versions of your application used full enum names like "Active", and you've now switched to abbreviations like "A", the `cons` function can support both:

- "Active" and "A" will both deserialize to `Status.Active`
- "Inactive" and "I" to `Status.Inactive`
- "Pending" and "P" to `Status.Pending`

This makes it easy to evolve your enum serialization format over time without breaking compatibility with existing data.

The custom converter can now be used just like any other in a `JAny` definition.

---

## 🧱 Sealed Classes

Kondor makes it easy to work with sealed class hierarchies by using the `JSealed` converter. This allows you to encode and decode subclasses with a discriminator field that indicates which type is being used.

### Example

```kotlin
sealed class Animal

data class Dog(val name: String) : Animal()
data class Cat(val lives: Int) : Animal()
```

You define converters for each concrete subclass:

```kotlin
object JDog : JAny<Dog>() {
    val name by str(Dog::name)

    override fun JsonNodeObject.deserializeOrThrow() =
        Dog(+name)
}

object JCat : JAny<Cat>() {
    val lives by num(Cat::lives)

    override fun JsonNodeObject.deserializeOrThrow() =
        Cat(+lives)
}
```

Then, define a `JSealed` converter for the sealed class. To do this, you need to override two things:

1. `extractTypeName(obj: T)`: a function that returns a string discriminator based on the type of the object.
2. `subConverters`: a map that links type names to their respective converters.

```kotlin
object JAnimal : JSealed<Animal>() {
    override fun extractTypeName(obj: Animal): String = when (obj) {
        is Dog -> "dog"
        is Cat -> "cat"
    }

    override val subConverters: Map<String, ObjectNodeConverter<out Animal>> = mapOf(
        "dog" to JDog,
        "cat" to JCat
    )
}
```

This will produce JSON like:

```json
{ "_type": "dog", "name": "Fido" }
```

When deserializing, Kondor uses the value of the `"_type"` property to determine which converter to use. This makes sealed class hierarchies both extensible and safe to work with in a polymorphic way.

By default, the discriminator property is named `_type`, but you can customize this by overriding:

```kotlin
override val discriminatorFieldName: String = "type"
```

As with enums, you can also map multiple different type strings to the same converter. This allows older JSON data to remain compatible even after renaming or restructuring the Kotlin class hierarchy. For instance:

```kotlin
override val subConverters = mapOf(
    "dog" to JDog,
    "canine" to JDog, // legacy support
    "cat" to JCat
)
```

This means both `{ "_type": "dog" }` and `{ "_type": "canine" }` will deserialize using `JDog`. This makes it easier to evolve sealed class hierarchies over time without breaking old clients.

---

## 📌 What's Next?

Explore more of Kondor’s features:

- 💻 [Converters Overview](getting-started.md) – an overview understanding how to work with Kondor converters
- 🔁 [Versioning the JSON](versioned-converter.md) – for evolving schemas
- 🔤 [Tiny Types (Value Objects)](tiny-types.md) – for handling object you want to represent as a string or number
- 🔤 [Enums and Sealed Classes](enums-and-sealed.md) – for working with enums and sealed classes
- 🛠 [Field Functions](short-field-functions.md) – helper functions for defining fields in Kondor converters
- 🧳 [Sealed Classes and Polymorphic JSON](sealed-classes.md) – handling polymorphic JSON with sealed classes
