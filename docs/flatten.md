# Unnesting with `flatten`

In Kondor, the `flatten` function allows you to flatten a nested data class (or Kotlin object) into a single-level JSON object. This function is particularly useful when you need to inline or "flatten" the fields of a nested object directly into the parent object’s JSON structure.

## How it works

When you're working with a nested Kotlin data class, you may want to represent the fields of the nested class as part of the parent object's JSON. The `flatten` function allows you to achieve this in a concise way.

Instead of having the nested object represented as a sub-object in the resulting JSON, the fields of the nested object will appear as top-level fields in the parent object.

### Example: Flattening a Nested Object

Consider the following Kotlin data classes:

```kotlin
data class Address(val street: String, val city: String)
data class User(val name: String, val email: String, val address: Address)
```

Without using `flatten`, the JSON for a `User` object might look like this:

```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "address": {
    "street": "123 Main St",
    "city": "Somewhere"
  }
}
```

But, by using `flatten`, you can inline the fields of the `Address` object directly into the parent `User` object.

### Using `flatten`

Here’s how you can use `flatten` to flatten the `address` field:

```kotlin
val userConverter = JAny { 
    str(User::name)
    str(User::email)
    flatten(User::address) { 
        str(Address::street)
        str(Address::city)
    }
}
```

This will produce the following JSON representation:

```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "street": "123 Main St",
  "city": "Somewhere"
}
```

As you can see, the `address` field has been flattened into `street` and `city` fields at the top level of the JSON.

## When to Use Flatten

Flattening is useful when you want to simplify the structure of your JSON without nesting objects. It can also help make your API's JSON more readable and intuitive by avoiding unnecessary levels of nesting.

## Summary

- The `flatten` function allows you to flatten nested data classes into a single-level JSON object.
- It inlines the fields of a nested object directly into the parent object’s JSON structure.
- Flattening is useful when you want to simplify your JSON or avoid nested structures.

In the next section, we’ll look at how to use `flatten` in combination with other Kondor functions to build complex converters.
