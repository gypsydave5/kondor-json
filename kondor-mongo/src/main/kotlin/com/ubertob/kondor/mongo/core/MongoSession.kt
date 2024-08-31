package com.ubertob.kondor.mongo.core

import com.mongodb.client.MongoCollection
import com.mongodb.client.model.*
import org.bson.BsonDocument
import org.bson.BsonValue
import org.bson.Document
import org.bson.conversions.Bson
import org.bson.types.ObjectId

interface MongoSession {

    val _logger: (String) -> Unit

    //General Methods
    fun listCollections(filter: Bson? = null): List<Document>

    fun listCollectionNames(filter: Bson? = null): List<String>

    //Edit Methods
    fun <T : Any> MongoTable<T>.insertOne(doc: T): BsonValue?
    fun <T : Any> MongoTable<T>.insertMany(docs: Iterable<T>): List<BsonValue>
    fun <T : Any> MongoTable<T>.deleteOne(bsonFilters: Bson): Long
    fun <T : Any> MongoTable<T>.deleteMany(bsonFilters: Bson): Long
    fun <T : Any> MongoTable<T>.replaceOne(filter: Bson, doc: T, options: ReplaceOptions = ReplaceOptions()): BsonValue?
    fun <T : Any> MongoTable<T>.findOneAndUpdate(
        bsonFilters: Bson,
        bsonSetter: Bson,
        options: FindOneAndUpdateOptions = RETURN_UPDATED
    ): T?

    fun <T : Any> MongoTable<T>.findOneAndReplace(
        bsonFilters: Bson,
        doc: T,
        options: FindOneAndReplaceOptions = UPSERT_RETURN_UPDATED
    ): T?

    fun <T : Any> MongoTable<T>.findOneAndDelete(
        bsonFilters: Bson,
        options: FindOneAndDeleteOptions = FindOneAndDeleteOptions()
    ): T?

    //!!! todo
//    fun <T : Any> MongoTable<T>.bulkWrite(
//        collection: List<T>,
//        options: BulkWriteOptions,
//        operation: (T) -> WriteModel<BsonDocument>
//    ): BulkWriteResult

    //Query Methods
    fun <T : Any> MongoTable<T>.findById(id: Any): T?
    fun <T : Any> MongoTable<T>.findByOid(id: ObjectId): T?
    fun <T : Any> MongoTable<T>.find(bsonFilters: Bson): Sequence<T>

    //    fun <T : Any, CONV: ObjectNodeConverter<T>> TypedTable<T, CONV>.find(filter: (CONV) -> Bson): Sequence<T>
    fun <T : Any> MongoTable<T>.find(queryString: String): Sequence<T>

    fun <T : Any> MongoTable<T>.all(): Sequence<T> = find("")
    fun <T : Any> MongoTable<T>.aggregate(vararg pipeline: Bson): Sequence<BsonDocument>

    fun MongoTable<*>.countDocuments(): Long

    //Table Methods
    fun <T : Any> MongoTable<T>.drop()
    fun <T : Any> MongoTable<T>.listIndexes(): Sequence<BsonDocument>


    //Useful for getting access to all drivers methods
    fun <T> MongoTable<*>.internalRun(block: (MongoCollection<BsonDocument>) -> T): T

    companion object {
        val RETURN_UPDATED = FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
        val RETURN_BEFORE = FindOneAndUpdateOptions().returnDocument(ReturnDocument.BEFORE)
        val UPSERT_RETURN_UPDATED = FindOneAndReplaceOptions().upsert(true).returnDocument(ReturnDocument.AFTER)
    }

    fun <T : Any> MongoTable<T>.updateOne(
        filter: Bson,
        updateModel: Bson,
        options: UpdateOptions = UpdateOptions()
    ): BsonValue?

    fun <T : Any> MongoTable<T>.updateMany(
        filter: Bson,
        updateModel: Bson,
        options: UpdateOptions = UpdateOptions()
    ): Long
}

typealias CollectionCache = MutableMap<String, MongoCollection<BsonDocument>>

