package io.zhudy.duic.repository

import io.zhudy.duic.config.transaction.TestReactiveTransactionAutoConfiguration
import io.zhudy.duic.repository.config.MongoTransactionConfiguration
import org.springframework.boot.autoconfigure.data.mongo.MongoReactiveDataAutoConfiguration
import org.springframework.boot.autoconfigure.mongo.MongoReactiveAutoConfiguration
import org.springframework.context.annotation.Import

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@Import(value = [
    MongoReactiveAutoConfiguration::class,
    MongoReactiveDataAutoConfiguration::class,
    MongoTransactionConfiguration::class,
    TestReactiveTransactionAutoConfiguration::class
])
class BasicTestMongoConfiguration