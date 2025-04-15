# Adding A Field

In Kondor, adding a field to a converter is generally done by using a set of helper functions that correspond to the basic JSON types. Each of these functions returns a `FieldConverter`, which can then be delegated to using Kotlin’s `by` keyword.

## The General Pattern

Each field in a `JAny` converter is typically defined using a short function named after the JSON type you're targeting. These functions take an accessor function as their main argument—usually a Kotlin property reference like `User::email`. Optionally, you can also pass a converter as a second argument, which will be used to handle conversion of the field’s value.

```kotlin
val email by str(User::email) // simple string field
val nickname by str(JNickname, User::nickname) // tiny type or custom converter
```

## Field Functions

Here are the most commonly used field functions:

---

### `str`

Use `str` to map a Kotlin `String` property to a JSON string field.

```kotlin
val name by str(Person::name)
```

If you're using a string-based tiny type (like `Email`), pass its converter in as the first argument:

```kotlin
val email by str(JEmail, User::email)
```

---

### `num`

Use `num` to map numeric Kotlin types (`Int`, `Long`, `Double`, etc.) to JSON number fields.

```kotlin
val age by num(Person::age)
```

If you're wrapping numeric types in tiny types, pass a custom converter first:

```kotlin
val score by num(JScore, Player::score)
```

---

### `bool`

Use `bool` to map a Kotlin `Boolean` property to a JSON boolean field.

```kotlin
val isActive by bool(User::isActive)
```

You can pass in a converter if you've wrapped the boolean in a tiny type:

```kotlin
val termsAccepted by bool(JTermsAccepted, SignupForm::termsAccepted)
```

---

### `array`

Use `array` to map collections (like `List` or `Set`) of other types to JSON arrays. You'll need to provide a converter for the element type.

```kotlin
val tags by array(JString, Article::tags)
```

This works for both `List<T>` and `Set<T>`.

---

### `obj`

Finally, we use `obj` to map a property that is another object (i.e. a nested data class) to a JSON object field. You'll need to provide a converter for the nested type.

```kotlin
val address by obj(JAddress, User::address)
```

---

These field helper functions form the foundation of how you describe the structure of your JSON in Kondor. With just a few of these, you can map most of your data models clearly and concisely.

---

## Summary Table

| Function | Kotlin Type                   | JSON Type | Notes                                                              |
| -------- | ----------------------------- | --------- | ------------------------------------------------------------------ |
| `str`    | `String`                      | string    | Direct string mapping. Use converter for wrapped types.            |
| `num`    | `Int`, `Long`, `Double`, etc. | number    | Supports all standard numeric types. Use converter for tiny types. |
| `bool`   | `Boolean`                     | boolean   | Can wrap with custom converters.                                   |
| `array`  | `List<T>`, `Set<T>`           | array     | Requires an element converter. Supports both List and Set.         |
| `obj`    | nested object (data class)    | object    | Requires a converter for the object.                               |

---

### One More: `flatten`

There’s one more function you can use when adding fields: `flatten`. This is used to flatten the fields of a nested object into the parent JSON object. We’ll explore this in more detail in a separate guide on [Flattening Fields](flatten.md).

---

## Field FAQs

### Why are we using the `by` Keyword?

The DSL relies on Kotlin's `by` keyword for delegation. Each field declaration like `val name by str(Person::name)` uses this keyword to assign the result of the `str()` function to a [delegated property](https://kotlinlang.org/docs/delegated-properties.html).

In this context, `str(Person::name)` returns a converter for that field, and the DSL uses delegation to register this converter with the containing `JAny` object.

Importantly, this delegation also gives the converter access to the *name of the property itself*. Kondor uses this information when serializing and deserializing to match the Kotlin property name with the corresponding field name in the JSON object.

This leads us to the next question...

### What if the JSON Property Name Isn't a Valid Kotlin Property Name?

Sometimes a field in the JSON uses characters or formats that aren't valid Kotlin property names—such as `"snake_case"`, `"kebab-case"`, or names with symbols. In these cases, you can still represent them in Kotlin by using **backticks** when declaring the property in your data class:

```kotlin
val `cover-letter` by str(User::coverLetter)
```

and we can carry on as usual.

---

## 📌 What's Next?

Explore more of Kondor’s features:

- 💻 [Converters Overview](getting-started.md) – an overview understanding how to work with Kondor converters
- 🔁 [Versioning the JSON](versioned-converter.md) – for evolving schemas
- 🔤 [Tiny Types (Value Objects)](tiny-types.md) – for handling object you want to represent as a string or number
- 🔤 [Enums and Sealed Classes](enums-and-sealed.md) – for working with enums and sealed classes
- 🛠 [Field Functions](short-field-functions.md) – helper functions for defining fields in Kondor converters
