package io.zhudy.duic.service

import io.zhudy.duic.config.BasicConfiguration
import io.zhudy.duic.repository.UserRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.web.ServerProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.security.crypto.password.PasswordEncoder

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@SpringBootTest(classes = [UserService::class])
@OverrideAutoConfiguration(enabled = false)
@ImportAutoConfiguration(classes = [
    BasicConfiguration::class
])
@EnableConfigurationProperties(ServerProperties::class)
class UserServiceTests {

    @MockBean
    lateinit var userRepository: UserRepository
    @SpyBean
    lateinit var passwordEncoder: PasswordEncoder

    //
    @Autowired
    lateinit var userService: UserService

    @Test
    fun login() {
        println("HELLO")
    }

}