package io.zhudy.duic.service

import io.zhudy.duic.domain.App
import io.zhudy.duic.repository.AppRepository
import org.springframework.stereotype.Service

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@Service
class AppService(val appRepository: AppRepository) {

    /**
     * @param app 应用实例
     */
    fun save(app: App) {
        appRepository.save(app)
    }

}