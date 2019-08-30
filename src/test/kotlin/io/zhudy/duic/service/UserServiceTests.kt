package io.zhudy.duic.service

import com.ninjasquad.springmockk.MockkBean
import com.ninjasquad.springmockk.SpykBean
import io.mockk.every
import io.zhudy.duic.BizCodes
import io.zhudy.duic.Config
import io.zhudy.duic.config.BasicConfiguration
import io.zhudy.duic.domain.Page
import io.zhudy.duic.domain.Pageable
import io.zhudy.duic.domain.User
import io.zhudy.duic.dto.ResetPasswordDto
import io.zhudy.duic.expectError
import io.zhudy.duic.bak.repository.UserRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.web.ServerProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.crypto.password.PasswordEncoder
import reactor.core.publisher.Mono
import reactor.core.publisher.toFlux
import reactor.core.publisher.toMono
import reactor.test.StepVerifier

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@SpringBootTest(classes = [
    UserService::class,
    BasicConfiguration::class
])
@EnableConfigurationProperties(ServerProperties::class)
internal class UserServiceTests {

    // ====================================== MOCK ============================================//
    @MockkBean
    lateinit var userRepository: UserRepository
    @SpykBean
    lateinit var passwordEncoder: PasswordEncoder
    // ====================================== MOCK ============================================//
    @Autowired
    lateinit var userService: UserService

    @Test
    fun init() {
        every { userRepository.findByEmail(Config.rootEmail) } returns Mono.empty()
        every { userRepository.insert(any()) } returns Mono.empty()

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
        every { userRepository.findByEmail(email) } returns user.toMono()

        val rs = userService.login(email, password)
        StepVerifier.create(rs)
                .expectNext(user)
                .verifyComplete()
    }

    @Test
    fun `login user not found`() {
        val email = "kevinz@weghst.com"
        val password = "hello"
        every { userRepository.findByEmail(email) } returns Mono.empty()

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
        every { userRepository.findByEmail(email) } returns user.toMono()

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
        every { userRepository.insert(user) } returns Mono.empty()

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
        every { userRepository.findByEmail(email) } returns mockUser.toMono()
        every { userRepository.updatePassword(any(), any()) } returns Mono.empty()

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
        every { userRepository.findByEmail(email) } returns mockUser.toMono()

        val rs = userService.updatePassword(email, oldPassword, newPassword)
        StepVerifier.create(rs)
                .expectError(BizCodes.C_2001)
                .verify()
    }

    @Test
    fun delete() {
        val email = "junit@weghst.com"
        every { userRepository.delete(email) } returns Mono.empty()

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
        every { userRepository.updatePassword(any(), any()) } returns Mono.empty()

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
        every { userRepository.updatePassword(any(), any()) } returns Mono.empty()

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
        every { userRepository.findPage(pageable) } returns page.toMono()

        val rs = userService.findPage(pageable)
        StepVerifier.create(rs)
                .expectNext(page)
                .expectComplete()
                .verify()
    }

    @Test
    fun findAllEmail() {
        val emails = arrayOf("kevinz@weghst.com")
        every { userRepository.findAllEmail() } returns emails.toFlux()

        val rs = userService.findAllEmail()
        StepVerifier.create(rs)
                .expectNext(*emails)
                .expectComplete()
                .verify()
    }
}