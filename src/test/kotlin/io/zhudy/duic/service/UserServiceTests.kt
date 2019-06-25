package io.zhudy.duic.service

import io.zhudy.duic.BizCodeException
import io.zhudy.duic.BizCodes
import io.zhudy.duic.config.BasicConfiguration
import io.zhudy.duic.domain.User
import io.zhudy.duic.repository.UserRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.web.ServerProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import reactor.test.StepVerifier

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@SpringBootTest(classes = [UserService::class])
@ActiveProfiles("test")
@OverrideAutoConfiguration(enabled = false)
@ImportAutoConfiguration(classes = [
    BasicConfiguration::class
])
@EnableConfigurationProperties(ServerProperties::class)
class UserServiceTests {

    @MockBean
    lateinit var userRepository: UserRepository
    @Autowired
    lateinit var passwordEncoder: PasswordEncoder
    @Autowired
    lateinit var userService: UserService

    @AfterEach
    fun clearMocks() {
        Mockito.framework().clearInlineMocks()
    }

    @Test
    fun login() {
        val email = "kevinz@weghst.com"
        val password = "hello"
        val user = User(
                email = email,
                password = passwordEncoder.encode(password)
        )
        given(userRepository.findByEmail(email)).willReturn(user.toMono())

        val rs = userService.login(email, password)
        StepVerifier.create(rs)
                .expectNext(user)
                .verifyComplete()
    }

    @Test
    fun `login user not found`() {
        val email = "kevinz@weghst.com"
        val password = "hello"
        given(userRepository.findByEmail(email)).willReturn(Mono.empty())

        val rs = userService.login(email, password)
        StepVerifier.create(rs)
                .expectErrorMatches {
                    if (it is BizCodeException) {
                        it.bizCode == BizCodes.C_2000
                    } else {
                        false
                    }
                }
                .verify()
    }

    @Test
    fun `login password not matches`() {
        val email = "kevinz@weghst.com"
        val password = "hello"
        val user = User(
                email = email,
                password = password
        )
        given(userRepository.findByEmail(email)).willReturn(user.toMono())

        val rs = userService.login(email, password)
        StepVerifier.create(rs)
                .expectErrorMatches {
                    if (it is BizCodeException) {
                        it.bizCode == BizCodes.C_2001
                    } else {
                        false
                    }
                }
                .verify()
    }

}