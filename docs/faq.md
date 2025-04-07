### Why does Kondor say it doesn't use reflection, even though it uses reflection to get the field names?

In Kondor, reflection is only used at the beginning when building the field mappings — specifically to capture the field names based on the Kotlin property references. However, once the field names are captured, Kondor operates without needing reflection during the actual encoding or decoding of JSON data.

This is an important distinction: while reflection is used to retrieve information about the field (e.g., the property name), it is not required during the conversion process itself. Instead, Kondor uses a set of pre-defined field mappings and converters, allowing it to operate efficiently without runtime reflection overhead when processing JSON.

Because of this approach, Kondor is able to offer the flexibility of reflection-based configuration (for capturing field names) while avoiding the performance hit that would come with using reflection during the actual JSON serialization and deserialization process.

This results in a highly efficient system that doesn't carry the usual reflection-based runtime cost, which is especially beneficial for performance-sensitive applications.
