# Core Concept

Unlike many serialization libraries, Kondor-JSON does not use intermediate DTOs. Instead, you define a single, bidirectional converter that maps your Kotlin type directly to and from JSON using `JConverter`.

Kondor internally represents JSON using a type called `JsonNode`. This is a tree-like structure that mirrors JSON syntax.

Most of the time, you don’t need to interact with a `JsonNode` directly — the DSL handles it for you via field bindings and converters.

Each converter is responsible for:

- Parsing a `JsonNode` into your type
- Serializing your type into a `JsonNode`

For the most part, this will mean converting objects, represented as JSON in Kondor as `JsonNodeObject`. We build an object converter for a type `T` by inheriting from `JAny<T>`.

## 📘 Key Takeaways

- 🚫 Kondor doesn't rely on intermediate DTOs. Instead, you define mapping functions that convert to and from JSON directly.
- 🤖 Much of the mapping logic is inferred automatically by the Kondor DSL.
- 📦 If you're unsure what kind of converter to use, `JAny` is likely the one you need—it's the most commonly used abstraction in the DSL.
