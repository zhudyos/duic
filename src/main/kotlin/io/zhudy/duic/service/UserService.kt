package io.zhudy.duic.service

import com.memeyule.cryolite.core.BizCodeException
import io.zhudy.duic.BizCodes
import io.zhudy.duic.Config
import io.zhudy.duic.domain.User
import io.zhudy.duic.dto.UpdatePasswordDto
import io.zhudy.duic.repository.UserRepository
import org.joda.time.DateTime
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.data.domain.Pageable
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@Service
class UserService(val userRepository: UserRepository,
                  val passwordEncoder: PasswordEncoder) {

    @EventListener
    fun listenContextStarted(event: ApplicationReadyEvent) {
        userRepository.findByEmail(Config.rootEmail).hasElement().subscribe {
            insert(User(email = Config.rootEmail, password = Config.rootPassword)).subscribe()
        }
    }

    /**
     * 用户登录.
     *
     * 错误返回
     * - [BizCodes.C_2000]
     * - [BizCodes.C_2001]
     *
     * @throws BizCodeException
     */
    fun login(email: String, password: String): Mono<User> {
        return userRepository.findByEmail(email).single().map {
            if (!passwordEncoder.matches(password, it!!.password)) {
                throw BizCodeException(BizCodes.C_2001)
            }
            it
        }.onErrorResume(NoSuchElementException::class.java) {
            throw BizCodeException(BizCodes.C_2000)
        }.cast(User::class.java)
    }

    /**
     * 保存用户.
     */
    fun insert(user: User): Mono<User> {
        user.password = passwordEncoder.encode(user.password)
        user.createdAt = DateTime.now()
        return userRepository.insert(user)
    }

    /**
     *
     */
    fun updatePassword(email: String, password: String) = userRepository.updatePassword(email, password)

    /**
     *
     */
    fun delete(email: String) = userRepository.delete(email)

    /**
     * 重置密码.
     */
    fun resetPassword(dto: UpdatePasswordDto) = userRepository.updatePassword(dto.email, passwordEncoder.encode(dto.password))

    /**
     *
     */
    fun findPage(page: Pageable) = userRepository.findPage(page)

    /**
     *
     */
    fun findAllEmail() = userRepository.findAllEmail()
}