package io.zhudy.duic.repository.impl

import io.zhudy.duic.domain.Page
import io.zhudy.duic.domain.Pageable
import io.zhudy.duic.domain.User
import io.zhudy.duic.repository.UserRepository
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.transaction.support.TransactionTemplate
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.toFlux
import reactor.core.publisher.toMono
import reactor.core.scheduler.Schedulers

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
class MySQLUserRepository(
        private val transactionTemplate: TransactionTemplate,
        private val jdbcTemplate: NamedParameterJdbcTemplate
) : UserRepository {

    override fun insert(user: User) = Mono.create<Int> {
        var n = 0
        transactionTemplate.execute {
            n = jdbcTemplate.update(
                    "INSERT INTO `user`(email,password,created_at) VALUES(:email,:password,:created_at)",
                    mapOf(
                            "email" to user.email,
                            "password" to user.password,
                            "created_at" to user.createdAt
                    )
            )
        }
        it.success(n)
    }.subscribeOn(Schedulers.elastic())

    override fun delete(email: String) = Mono.create<Int> {
        transactionTemplate.execute {
            jdbcTemplate.update(
                    "DELETE FROM `user` WHERE `email`=:email",
                    mapOf(
                            "email" to email
                    )
            )
        }
    }.subscribeOn(Schedulers.elastic())

    override fun updatePassword(email: String, password: String) = Mono.create<Int> {
        transactionTemplate.execute {
            jdbcTemplate.update(
                    "UPDATE `user` SET `password`=:password,`updated_at`=now() WHERE `email`=:email",
                    mapOf(
                            "email" to email,
                            "password" to password
                    )
            )
        }
    }.subscribeOn(Schedulers.elastic())

    override fun findByEmail(email: String) = Mono.create<User> {
        var user: User? = null
        transactionTemplate.execute {
            jdbcTemplate.query(
                    "SELECT `email`,`password` FROM `user` WHERE `email`=:email",
                    mapOf(
                            "email" to email
                    ), {
                user = User(
                        email = it.getString(0),
                        password = it.getString(1)
                )
            }
            )
        }
        it.success(user)
    }

    override fun findPage(pageable: Pageable) = Mono.zip(
            Mono.create<List<User>> {
                transactionTemplate.execute {
                    jdbcTemplate.queryForList(
                            "SELECT `email`,`created_at`,`updated_at` FROM `user` LIMIT :offset,:limit",
                            mapOf(
                                    "offset" to pageable.offset,
                                    "limit" to pageable.offset + pageable.size
                            ),
                            User::class.java
                    )
                }
            },
            Mono.create<Int> {
                transactionTemplate.execute {
                    jdbcTemplate.query(
                            "SELECT COUNT(1) FROM `user`",
                            {
                                it.getInt(0)
                            }
                    )
                }
            }
    ).map {
        Page(it.t1, it.t2, pageable)
    }

    override fun findAllEmail() = Flux.create<String> { sink ->
        val list = mutableListOf<String>()
        transactionTemplate.execute {
            jdbcTemplate.query(
                    "SELECT `email` FROM `user`",
                    {
                        list.add(it.getString(0))
                    }
            )
        }
        list.forEach { sink.next(it) }
    }

}