package io.zhudy.duic.repository.postgresql

import io.zhudy.duic.repository.AbstractRelationalUserRepository
import org.springframework.data.r2dbc.core.DatabaseClient

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
class PostgreSqlUserRepositoryImpl(dc: DatabaseClient) : AbstractRelationalUserRepository(dc)