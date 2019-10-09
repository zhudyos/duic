package io.zhudy.duic.repository.mongo

import io.zhudy.duic.dto.NewUserDto
import io.zhudy.duic.repository.BasicTestMongoConfiguration
import io.zhudy.duic.repository.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.DuplicateKeyException
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.reactive.TransactionalOperator
import reactor.core.publisher.Mono
import java.util.*

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@SpringBootTest(classes = [
    MongoUserRepositoryImpl::class
])
@ActiveProfiles("mongodb")
@ImportAutoConfiguration(classes = [BasicTestMongoConfiguration::class])
internal class MongoUserRepositoryImplTests {

    @Autowired
    private lateinit var userRepository: UserRepository
    @Autowired
    private lateinit var transactionalOperator: TransactionalOperator

    @Test
    fun insert() {
        val email = "integration-test@mail.com"
        val password = "[PASSWORD]"

        val p = userRepository.insert(NewUserDto(email = email, password = password))
        val n = transactionalOperator.transactional(p).block()
        assertThat(n).isEqualTo(1)
    }

    @Test
    fun `insert duplicate`() {
        val email = "integration-test@mail.com"
        val password = "[PASSWORD]"

        val p = userRepository.insert(NewUserDto(email = email, password = password))
                .then(
                        userRepository.insert(NewUserDto(email = email, password = password))
                )

        assertThatThrownBy { transactionalOperator.transactional(p).block() }
                .isInstanceOf(DuplicateKeyException::class.java)
                .hasMessageContaining("Duplicate")
    }

    @Test
    fun delete() {
        val email = "integration-test@mail.com"
        val password = "[PASSWORD]"

        val p = userRepository.insert(NewUserDto(email = email, password = password))
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

        val p = userRepository.insert(NewUserDto(email = email, password = password))
                .then(
                        userRepository.updatePassword(email, "[NEW-PASSWORD]")
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

        val p = userRepository.insert(NewUserDto(email = email, password = password))
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
    fun findPage() {
        val c = 30
        var prepare = Mono.empty<Int>()
        for (i in 1..30) {
            val dto = NewUserDto(email = "integration-test$i@mail.com", password = "[PASSWORD]")
            prepare = prepare.then(userRepository.insert(dto))
        }

        val pageRequest = PageRequest.of(0, 15)
        val p = prepare.then(userRepository.findPage(pageRequest))

        val page = transactionalOperator.transactional(p).block()
        assertThat(page).isNotNull
        assertThat(page.totalElements).isGreaterThanOrEqualTo(c.toLong())
    }

    @Test
    fun findAllEmail() {
        val c = 30
        var prepare = Mono.empty<Int>()
        for (i in 1..30) {
            val dto = NewUserDto(email = "integration-test$i@mail.com", password = "[PASSWORD]")
            prepare = prepare.then(userRepository.insert(dto))
        }

        val p = prepare.thenMany(userRepository.findAllEmail())
        val list = transactionalOperator.transactional(p).collectList().block()
        assertThat(list).hasSizeGreaterThanOrEqualTo(c)
    }
}