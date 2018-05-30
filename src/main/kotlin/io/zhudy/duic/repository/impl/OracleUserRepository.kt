package io.zhudy.duic.repository.impl

import io.zhudy.duic.domain.Page
import io.zhudy.duic.domain.Pageable
import io.zhudy.duic.domain.User
import io.zhudy.duic.repository.AbstractTransactionRepository
import io.zhudy.duic.repository.UserRepository
import org.springframework.jdbc.core.ResultSetExtractor
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.transaction.PlatformTransactionManager
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
class OracleUserRepository(
        transactionManager: PlatformTransactionManager,
        private val jdbcTemplate: NamedParameterJdbcTemplate
) : UserRepository, AbstractTransactionRepository(transactionManager) {

    override fun insert(user: User) = Mono.create<Int> {
        val n = transactionTemplate.execute {
            jdbcTemplate.update(
                    "INSERT INTO T_USER(id,email,password,created_at) VALUES(SEQ_USER.nextval,:email,:password,CURRENT_TIMESTAMP)",
                    mapOf(
                            "email" to user.email,
                            "password" to user.password
                    )
            )
        }
        it.success(n)
    }.subscribeOn(Schedulers.elastic())

    override fun delete(email: String) = Mono.create<Int> {
        val n = transactionTemplate.execute {
            jdbcTemplate.update(
                    "DELETE FROM T_USER WHERE email=:email",
                    mapOf("email" to email)
            )
        }
        it.success(n)
    }.subscribeOn(Schedulers.elastic())

    override fun updatePassword(email: String, password: String) = Mono.create<Int> {
        val n = transactionTemplate.execute {
            jdbcTemplate.update(
                    "UPDATE T_USER SET password=:password,updated_at=CURRENT_TIMESTAMP WHERE email=:email",
                    mapOf("email" to email, "password" to password)
            )
        }
        it.success(n)
    }.subscribeOn(Schedulers.elastic())

    override fun findByEmail(email: String) = Mono.create<User> { sink ->
        roTransactionTemplate.execute {
            jdbcTemplate.query(
                    "SELECT email,password FROM T_USER WHERE email=:email",
                    mapOf("email" to email),
                    ResultSetExtractor {
                        if (it.next()) {
                            sink.success(User(it.getString(1), it.getString(2)))
                        } else {
                            sink.success()
                        }
                    }
            )
        }
    }.subscribeOn(Schedulers.elastic())

    override fun findPage(pageable: Pageable) = Mono.zip(
            Flux.create<User> { sink ->
                roTransactionTemplate.execute {
                    jdbcTemplate.query(
                            "SELECT * FROM ( SELECT A.*, ROWNUM RN FROM (SELECT email,created_at,updated_at FROM T_USER) A WHERE ROWNUM <= :e ) WHERE RN >= :b",
                            mapOf("b" to pageable.begin, "e" to pageable.end)
                    ) {
                        sink.next(User(
                                email = it.getString(1),
                                createdAt = it.getTimestamp(2),
                                updatedAt = it.getTimestamp(3)
                        ))
                    }
                    sink.complete()
                }
            }.subscribeOn(Schedulers.elastic()).collectList(),
            Mono.create<Int> {
                val c = roTransactionTemplate.execute {
                    jdbcTemplate.queryForObject(
                            "SELECT COUNT(1) FROM T_USER",
                            EmptySqlParameterSource.INSTANCE,
                            Int::class.java
                    )
                }
                it.success(c)
            }.subscribeOn(Schedulers.elastic())
    ).map {
        Page(it.t1, it.t2, pageable)
    }

    override fun findAllEmail() = Flux.create<String> { sink ->
        roTransactionTemplate.execute {
            jdbcTemplate.query("SELECT email FROM T_USER") {
                sink.next(it.getString(1))
            }
            sink.complete()
        }
    }.subscribeOn(Schedulers.elastic())

}