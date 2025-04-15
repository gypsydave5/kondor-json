# ✨ `JAny`: The General Object Converter

In Kondor, `JAny` is the primary converter used for serializing and deserializing entire objects. When you are working with complex objects in your Kotlin code, `JAny` provides a convenient way to map these objects to and from their JSON representations.

A `JAny` converter works by mapping Kotlin properties to JSON fields, using the various [field functions](short-field-functions.md) (such as `str`, `num`, `bool`, etc.) to handle individual properties within the object.

We work with `JAny` by extending from it - either as a `class` or, more usually, an `object`.

Let’s define a `Person` class and create a converter by extending `JAny<Person>`.

```kotlin
import com.ubertob.kondor.json.*
import com.ubertob.kondor.json.jvalue.*

data class Person(val name: String, val age: Int)

object JPerson : JAny<Person>() {
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

📝 **Convention Note**: While we’ve named our converter `JPerson` here, this is just a Kondor convention. Feel free to
name your converter in any way that will help you identify it in your code.

---

## 🧠 What's Going On?

### 🔑 Defining Fields

We start by defining fields using the DSL delegates like `str` and `num`. These fields form the shape of the JSON representation.

The **names you give these fields in your converter directly become the property names** in the serialized JSON object.

For example, in the converter we define `val name by str(Person::name)`, so the resulting JSON will include a property named `"name"`. If you changed this to `val fullName by str(Person::name)`, the output JSON would have `"fullName"` instead. We use the `by` keyword to enable this.

Take a look at the [section on adding fields](short-field-functions.md) for more information.

### `JsonNodeObject.deserializeOrThrow()`

This extension function is where the work of deserializing goes on. It should return the deserialized type (a `Person` in this case) - or throw an exception. We can use our defined fields in this function to perform the deserialization of the individual properties.

### ➕ The Bind Operator

We use the `+` operator (also known as the *bind operator*) to extract values from the `JsonNodeObject`.

For example:

```kotlin
name = +name
```

This line binds the value of the JSON `"name"` field (as defined by the delegate) into the `name` property of the `Person` object.

If it fails, for whatever reason, it will throw an exception.

## 📌 What's Next?

Once you're comfortable with this approach, explore more advanced features:

- 🔁 [VersionedConverter](versioned-converter.md) – for evolving schemas
- 🔤 [JStringRepresentable](jstring-representable.md) – for enums and string-based types
- 🧱 [Tiny Types](tiny-types.md) – using value classes and domain wrappers

