# Field Functions

When defining a converter in Kondor using `JAny`, you'll typically use one of a set of helper functions to define each field.

These helper functions are named after the JSON type you're mapping to – like `str` for strings, `num` for numbers, and so on.

Each function takes at least one argument: a property accessor (like `User::email`) that tells Kondor which field to serialize.

Optionally, you can provide a converter as the first argument — for example, if you're wrapping a field in a tiny type or using a custom format. This gives you flexibility to encode domain-specific types or legacy formats cleanly.

Here are the most commonly used field functions:

---

## `str`

Use `str` to map a Kotlin `String` property to a JSON string field.

```kotlin
val name by str(Person::name)
```

If you're using a string-based tiny type (like `Email`), pass its converter in as the first argument:

```kotlin
val email by str(JEmail, User::email)
```

---

## `num`

Use `num` to map numeric Kotlin types (`Int`, `Long`, `Double`, etc.) to JSON number fields.

```kotlin
val age by num(Person::age)
```

If you're wrapping numeric types in tiny types, pass a custom converter first:

```kotlin
val score by num(JScore, Player::score)
```

---

## `bool`

Use `bool` to map a Kotlin `Boolean` property to a JSON boolean field.

```kotlin
val isActive by bool(User::isActive)
```

You can pass in a converter if you've wrapped the boolean in a tiny type:

```kotlin
val termsAccepted by bool(JTermsAccepted, SignupForm::termsAccepted)
```

---

## `array`

Use `array` to map collections (like `List` or `Set`) of other types to JSON arrays. You'll need to provide a converter for the element type.

```kotlin
val tags by array(JString, Article::tags)
```

This works for both `List<T>` and `Set<T>`.

---

## `obj`

Finally, we use `obj` to map a property that is another object (i.e. a nested data class) to a JSON object field. You'll need to provide a converter for the nested type.

```kotlin
val address by obj(JAddress, User::address)
```

---

These field helper functions form the foundation of how you describe the structure of your JSON in Kondor. With just a few of these, you can map most of your data models clearly and concisely.

There’s also one more field builder function available: `flatten`. This is used when you need to inline or "flatten" the fields of a nested object directly into the parent object’s JSON structure. We’ll look at how and when to use this in a separate part of the documentation.

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

## 📌 What's Next?

Explore more of Kondor’s features:

- 💻 [Converters Overview](getting-started.md) – an overview understanding how to work with Kondor converters
- 🔁 [Versioning the JSON](versioned-converter.md) – for evolving schemas
- 🔤 [Tiny Types (Value Objects)](tiny-types.md) – for handling object you want to represent as a string or number
- 🔤 [Enums and Sealed Classes](enums-and-sealed.md) – for working with enums and sealed classes
- 🛠 [Field Functions](short-field-functions.md) – helper functions for defining fields in Kondor converters
- 🧳 [Sealed Classes and Polymorphic JSON](sealed-classes.md) – handling polymorphic JSON with sealed classes
