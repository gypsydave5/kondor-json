# Error Handling in Kondor

Kondor aims to give you type-safe, declarative control over your JSON conversions — and that includes how errors are handled.

## Encoding Is Always Safe

When **encoding** an object to JSON using Kondor, you don't need to worry about runtime errors. The type system ensures that your object is valid for the converter you're using. So, `toJson(...)` always succeeds:

```kotlin
val json = JUser.toJson(User("alice@example.com"))
```

## Decoding May Fail

However, when **decoding** JSON, things can go wrong — malformed input, missing fields, or incompatible types. For this, Kondor provides a safe and explicit way to handle errors: the `fromJson` function returns an `Outcome`.

```kotlin
val result: Outcome<User, JsonError> = JUser.fromJson(json)
```

## What Is an `Outcome`?

`Outcome` is a sealed type that represents either:

- a `Success<T>` containing the parsed object, or
- a `Failure<E>` containing a `JsonError`.

This is similar to the `Either` type from languages like Scala or Haskell.

### Quick and Dirty: `.orThrow()` and `.orNull()`

If you're sure your input is correct (e.g. in a test or simple script), you can get the decoded object directly using:

```kotlin
val user: User = JUser.fromJson(json).orThrow()
```

This will throw an exception if decoding fails — which may be fine in trusted or test environments.

Alternatively, if you prefer a null fallback:

```kotlin
val user: User? = JUser.fromJson(json).orNull()
```

This will return `null` instead of throwing if decoding fails.

## Handling Outcome Explicitly

In more robust code, you should explicitly handle both success and failure. Kondor’s `Outcome` provides methods for this.

### `transform` and `recover`

- Use `transform` to map the success value:

```kotlin
val emailLength = outcome.transform { user -> user.email.length }
```

- Use `recover` to fix up an error (if there is one) and return a fallback success value:

```kotlin
val fallbackLength = outcome.recover { error -> 0 }
```

- A combination of the two covers most eventualities

```kotlin
val email = JUser.fromJson(json)
              .transform { user -> user.email }
              .recover { "default@somewhere.com" }
```

### Pattern Matching

```kotlin
when (val result = JUser.fromJson(json)) {
    is Outcome.Success -> println("User: ${result.value}")
    is Outcome.Failure -> println("Error: ${result.reason}")
}
```

## Converting to Other Result Types

If you’re using a different result-handling library, such as [result4k](https://github.com/fork-handles/forkhandles/blob/trunk/result4k), you can easily convert an `Outcome<T, E>` into a `Result<T, E>`:

```kotlin
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.Failure

fun <T, E> Outcome<T, E>.toResult4k(): Result<T, E> = when (this) {
    is Outcome.Success -> Result.Success(value)
    is Outcome.Failure -> Result.Failure(reason)
}
```

Now you can interoperate seamlessly:

```kotlin
val result4k: Result<User, JsonError> = JUser.fromJson(json).toResult4k()
```

## Summary

- Encoding with Kondor is always safe.
- Decoding uses `Outcome`, which is a functional way to deal with possible errors.
- You can throw, handle, or convert `Outcome` depending on your use case.

Kondor doesn’t just help you build safe JSON codecs — it also helps you **fail safely**.

