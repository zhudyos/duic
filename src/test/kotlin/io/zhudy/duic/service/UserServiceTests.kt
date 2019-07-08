package io.zhudy.duic.service

import io.zhudy.duic.BizCodes
import io.zhudy.duic.Config
import io.zhudy.duic.config.BasicConfiguration
import io.zhudy.duic.domain.Page
import io.zhudy.duic.domain.Pageable
import io.zhudy.duic.domain.User
import io.zhudy.duic.dto.ResetPasswordDto
import io.zhudy.duic.expectError
import io.zhudy.duic.repository.UserRepository
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
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
import reactor.core.publisher.toFlux
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
internal class UserServiceTests {

    // ====================================== MOCK ============================================//
    @MockBean
    lateinit var userRepository: UserRepository
    @SpyBean
    lateinit var passwordEncoder: PasswordEncoder
    // ====================================== MOCK ============================================//
    @Autowired
    lateinit var userService: UserService

    @Test
    fun init() {
        given(userRepository.findByEmail(Config.rootEmail)).willReturn(Mono.empty())
        given(userRepository.insert(any() ?: User())).willReturn(Mono.empty())

        userService.Lifecycle().init()
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
                .expectComplete()
                .verify()
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

    @Test
    fun delete() {
        val email = "junit@weghst.com"
        given(userRepository.delete(email)).willReturn(Mono.empty())

        val rs = userService.delete(email)
        StepVerifier.create(rs)
                .verifyComplete()
    }

    @Test
    fun `delete root user`() {
        val rs = userService.delete(Config.rootEmail)
        StepVerifier.create(rs)
                .expectError(BizCodes.C_2002)
                .verify()
    }

    @Test
    fun resetPassword() {
        val dto = ResetPasswordDto(
                email = "junit@weghst.com",
                password = "hello"
        )
        given(userRepository.updatePassword(anyString(), anyString())).willReturn(Mono.empty())

        val rs = userService.resetPassword(dto)
        StepVerifier.create(rs)
                .expectComplete()
                .verify()
    }

    @Test
    fun `resetPassword root user`() {
        val dto = ResetPasswordDto(
                email = Config.rootEmail,
                password = "hello"
        )
        given(userRepository.updatePassword(anyString(), anyString())).willReturn(Mono.empty())

        val rs = userService.resetPassword(dto)
        StepVerifier.create(rs)
                .expectError(BizCodes.C_2003)
                .verify()
    }

    @Test
    fun findPage() {
        val pageable = Pageable()
        val page = Page<User>(
                pageable = pageable
        )
        given(userRepository.findPage(pageable)).willReturn(page.toMono())

        val rs = userService.findPage(pageable)
        StepVerifier.create(rs)
                .expectNext(page)
                .expectComplete()
                .verify()
    }

    @Test
    fun findAllEmail() {
        val emails = arrayOf("kevinz@weghst.com")
        given(userRepository.findAllEmail()).willReturn(emails.toFlux())

        val rs = userService.findAllEmail()
        StepVerifier.create(rs)
                .expectNext(*emails)
                .expectComplete()
                .verify()
    }
}