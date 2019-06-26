package io.zhudy.duic.service

import io.zhudy.duic.BizCodes
import io.zhudy.duic.config.BasicConfiguration
import io.zhudy.duic.domain.User
import io.zhudy.duic.expectError
import io.zhudy.duic.repository.UserRepository
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.web.ServerProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.SpyBean
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

    // ====================================== MOCK ============================================//
    @MockBean
    lateinit var userRepository: UserRepository
    @SpyBean
    lateinit var passwordEncoder: PasswordEncoder
    // ====================================== MOCK ============================================//
    @Autowired
    lateinit var userService: UserService

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
                .expectError(BizCodes.C_2000)
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
                .expectError(BizCodes.C_2001)
                .verify()
    }

    @Test
    fun insert() {
        val email = "kevinz@weghst.com"
        val password = "hello"
        val user = User(
                email = email,
                password = password
        )
        given(userRepository.insert(user)).willReturn(Mono.empty())

        val rs = userService.insert(user)
        StepVerifier.create(rs)
                .expectComplete()
                .verify()
    }

    @Test
    fun updatePassword() {
        val email = "kevinz@weghst.com"
        val oldPassword = "hello"
        val newPassword = "new-hello"

        val mockUser = User(
                email = email,
                password = passwordEncoder.encode(oldPassword)
        )
        given(userRepository.findByEmail(email)).willReturn(mockUser.toMono())
        given(userRepository.updatePassword(anyString(), anyString())).willReturn(Mono.empty())

        val rs = userService.updatePassword(email, oldPassword, newPassword)
        StepVerifier.create(rs)
                .verifyComplete()
    }

    @Test
    fun `updatePassword password not matches`() {
        val email = "kevinz@weghst.com"
        val oldPassword = "hello"
        val newPassword = "new-hello"

        val mockUser = User(
                email = email,
                password = passwordEncoder.encode("$oldPassword-error")
        )
        given(userRepository.findByEmail(email)).willReturn(mockUser.toMono())

        val rs = userService.updatePassword(email, oldPassword, newPassword)
        StepVerifier.create(rs)
                .expectError(BizCodes.C_2001)
                .verify()
    }

}