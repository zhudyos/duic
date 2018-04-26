package io.zhudy.duic.repository.impl

import io.zhudy.duic.domain.Page
import io.zhudy.duic.domain.Pageable
import io.zhudy.duic.domain.User
import io.zhudy.duic.repository.UserRepository
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.transaction.support.TransactionTemplate
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
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

    override fun delete(email: String): Mono<Int> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updatePassword(email: String, password: String): Mono<Int> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun findByEmail(email: String): Mono<User> {
        return Mono.empty()
    }

    override fun findPage(pageable: Pageable): Mono<Page<User>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun findAllEmail(): Flux<String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}