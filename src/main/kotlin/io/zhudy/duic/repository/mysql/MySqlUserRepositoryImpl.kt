package io.zhudy.duic.repository.mysql

import io.zhudy.duic.repository.AbstractRelationalUserRepository
import org.springframework.data.r2dbc.core.DatabaseClient

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
class MySqlUserRepositoryImpl(dc: DatabaseClient) : AbstractRelationalUserRepository(dc)