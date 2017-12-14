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

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@Service
class UserService(val userRepository: UserRepository,
                  val passwordEncoder: PasswordEncoder) {

    @EventListener
    fun listenContextStarted(event: ApplicationReadyEvent) {
        val root = userRepository.findByEmailOrNull(Config.rootEmail)
        if (root == null) {
            save(User(email = Config.rootEmail, password = Config.rootPassword))
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
    fun login(email: String, password: String): User {
        val user = userRepository.findByEmailOrNull(email) ?: throw BizCodeException(BizCodes.C_2000, "未找到用户 $email")
        if (!passwordEncoder.matches(password, user.password)) {
            throw BizCodeException(BizCodes.C_2001)
        }
        return user
    }

    /**
     * 保存用户.
     */
    fun save(user: User) {
        user.password = passwordEncoder.encode(user.password)
        user.createdAt = DateTime.now()
        userRepository.save(user)
    }

    /**
     *
     */
    fun updatePassword(email: String, password: String) = userRepository.updatePassword(email, password)

    /**
     *
     */
    fun delete(email: String) {
        userRepository.delete(email)
    }

    /**
     * 重置密码.
     */
    fun resetPassword(dto: UpdatePasswordDto) = userRepository.updatePassword(dto.email, passwordEncoder.encode(dto.password))

    /**
     *
     */
    fun findPage(page: Pageable) = userRepository.findPage(page)

    fun findAllEmail() = userRepository.findAllEmail()
}