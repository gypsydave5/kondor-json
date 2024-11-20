package com.ubertob.kondor.mongo.core

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.connection.ClusterSettings
import java.time.Duration
import java.util.concurrent.TimeUnit

data class MongoConnection(val connString: String, val timeout: Duration = Duration.ofMillis(500)) {
    fun toMongoClientSettings(): MongoClientSettings = MongoClientSettings.builder()
        .applyToSocketSettings { builder ->
            builder.applySettings(
                builder.connectTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS).build()
            )
        }
        .applyToClusterSettings { builder: ClusterSettings.Builder ->
            builder.serverSelectionTimeout(
                timeout.toMillis(),
                TimeUnit.MILLISECONDS
            ).build()
        }
        .applyConnectionString(ConnectionString(connString))
        .build()
}


