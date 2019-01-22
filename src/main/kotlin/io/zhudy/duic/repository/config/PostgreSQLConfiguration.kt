/**
 * Copyright 2017-2018 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.zhudy.duic.repository.config

import io.zhudy.duic.repository.impl.PostgreSQLAppRepository
import io.zhudy.duic.repository.impl.PostgreSQLServerRepository
import io.zhudy.duic.repository.impl.PostgreSQLUserRepository
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.transaction.PlatformTransactionManager

/**
 * `PostgreSQL` 配置。
 *
 * @author Kevin Zou (kevinz@weghst.com)
 */
@Configuration
@ConditionalOnExpression("T(io.zhudy.duic.DBMS).forName('\${duic.dbms}') == T(io.zhudy.duic.DBMS).PostgreSQL")
class PostgreSQLConfiguration {

    @Bean
    fun userRepository(transactionManager: PlatformTransactionManager, jdbcTemplate: NamedParameterJdbcTemplate)
            = PostgreSQLUserRepository(transactionManager, jdbcTemplate)

    @Bean
    fun appRepository(transactionManager: PlatformTransactionManager, jdbcTemplate: NamedParameterJdbcTemplate)
            = PostgreSQLAppRepository(transactionManager, jdbcTemplate)

    @Bean
    fun serverRepository(transactionManager: PlatformTransactionManager, jdbcTemplate: NamedParameterJdbcTemplate)
            = PostgreSQLServerRepository(transactionManager, jdbcTemplate)

}