package io.zhudy.duic.repository.mysql

import io.zhudy.duic.repository.AbstractUserRepository
import org.springframework.data.r2dbc.core.DatabaseClient

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
class MySqlUserRepositoryImpl(dc: DatabaseClient) : AbstractUserRepository(dc)