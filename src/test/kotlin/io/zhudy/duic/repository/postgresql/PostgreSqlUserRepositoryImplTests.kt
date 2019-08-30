package io.zhudy.duic.repository.postgresql

import io.r2dbc.spi.R2dbcDataIntegrityViolationException
import io.zhudy.duic.domain.User
import io.zhudy.duic.repository.BasicTestRelationalConfiguration
import io.zhudy.duic.repository.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.reactive.TransactionalOperator
import reactor.core.publisher.Flux
import java.util.*

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@SpringBootTest(classes = [
    PostgreSqlUserRepositoryImpl::class
])
@ActiveProfiles("postgresql")
@ImportAutoConfiguration(classes = [BasicTestRelationalConfiguration::class])
internal class PostgreSqlUserRepositoryImplTests {

    @Autowired
    private lateinit var userRepository: UserRepository
    @Autowired
    private lateinit var transactionalOperator: TransactionalOperator

    @Test
    fun insert() {
        val email = "integration-test@mail.com"
        val password = "[PASSWORD]"

        val p = userRepository.insert(User(email = email, password = password))
        val n = transactionalOperator.transactional(p).block()
        assertThat(n).isEqualTo(1)
    }

    @Test
    fun `insert duplicate`() {
        val email = "integration-test@mail.com"
        val password = "[PASSWORD]"

        val p = userRepository.insert(User(email = email, password = password))
                .then(
                        userRepository.insert(User(email = email, password = password))
                )

        assertThatThrownBy { transactionalOperator.transactional(p).block() }
                .isInstanceOf(DataIntegrityViolationException::class.java)
                .hasCauseInstanceOf(R2dbcDataIntegrityViolationException::class.java)
                .hasMessageContaining("Duplicate")
    }

    @Test
    fun delete() {
        val email = "integration-test@mail.com"
        val password = "[PASSWORD]"

        val p = userRepository.insert(User(email = email, password = password))
                .then(
                        userRepository.delete(email)
                )
        val n = transactionalOperator.transactional(p).block()
        assertThat(n).isEqualTo(1)
    }

    @Test
    fun `delete not found`() {
        val email = "${UUID.randomUUID()}@mail.com"

        val p = userRepository.delete(email)
        val n = transactionalOperator.transactional(p).block()
        assertThat(n).isEqualTo(0)
    }

    @Test
    fun updatePassword() {
        val email = "integration-test@mail.com"
        val password = "[PASSWORD]"

        val p = userRepository.insert(User(email = email, password = password))
                .then(
                        userRepository.updatePassword(email, password)
                )
        val n = transactionalOperator.transactional(p).block()
        assertThat(n).isEqualTo(1)
    }

    @Test
    fun `updatePassword not found`() {
        val email = "${UUID.randomUUID()}@mail.com"
        val password = "[PASSWORD]"

        val p = userRepository.updatePassword(email, password)
        val n = transactionalOperator.transactional(p).block()
        assertThat(n).isEqualTo(0)
    }

    @Test
    fun findByEmail() {
        val email = "integration-test@mail.com"
        val password = "[PASSWORD]"

        val p = userRepository.insert(User(email = email, password = password))
                .then(
                        userRepository.findByEmail(email)
                )
        val u = transactionalOperator.transactional(p).block()
        assertThat(u).isNotNull
                .hasFieldOrPropertyWithValue("password", password)
                .hasFieldOrProperty("createdAt")
                .hasFieldOrProperty("updatedAt")
    }

    @Test
    fun `findByEmail not found`() {
        val email = "integration-test@mail.com"

        val p = userRepository.findByEmail(email)
        val u = transactionalOperator.transactional(p).block()
        assertThat(u).isNull()
    }

    @Test
    fun findPage() {
        val c = 30
        val pageRequest = PageRequest.of(0, 15)

        val prepare = Flux.range(0, c).map {
            User(email = "integration-test$it@mail.com", password = "[PASSWORD]")
        }.flatMap(userRepository::insert)

        val p = prepare.then(userRepository.findPage(pageRequest))

        val page = transactionalOperator.transactional(p).block()
        assertThat(page).isNotNull
        assertThat(page.totalElements).isGreaterThanOrEqualTo(c.toLong())
    }

    @Test
    fun findAllEmail() {
        val c = 30
        val prepare = Flux.range(0, c).map {
            User(email = "integration-test$it@mail.com", password = "[PASSWORD]")
        }.flatMap(userRepository::insert)

        val p = prepare.thenMany(userRepository.findAllEmail())
        val list = transactionalOperator.transactional(p).collectList().block()
        assertThat(list).hasSizeGreaterThanOrEqualTo(c)
    }

}