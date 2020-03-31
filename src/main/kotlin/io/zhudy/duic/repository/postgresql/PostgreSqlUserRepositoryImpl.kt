package io.zhudy.duic.repository.postgresql

import io.zhudy.duic.repository.AbstractUserRepository
import org.springframework.data.r2dbc.core.DatabaseClient

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
class PostgreSqlUserRepositoryImpl(dc: DatabaseClient) : AbstractUserRepository(dc)