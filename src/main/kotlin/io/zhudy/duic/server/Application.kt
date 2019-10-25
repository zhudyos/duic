/**
 * Copyright 2017-2019 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.zhudy.duic.server

import io.zhudy.duic.ApplicationUnusableEvent
import io.zhudy.duic.ApplicationUsableEvent
import org.springframework.boot.ExitCodeGenerator
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.dao.PersistenceExceptionTranslationAutoConfiguration
import org.springframework.boot.autoconfigure.http.codec.CodecsAutoConfiguration
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration
import org.springframework.boot.autoconfigure.web.reactive.error.ErrorWebFluxAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationListener


/**
 * 程序入口。
 *
 * @author Kevin Zou (kevinz@weghst.com)
 */
@SpringBootApplication(
        scanBasePackages = [
            "io.zhudy.duic"
        ],
        exclude = [
            RestTemplateAutoConfiguration::class,
            ErrorWebFluxAutoConfiguration::class,
            CodecsAutoConfiguration::class,
            PersistenceExceptionTranslationAutoConfiguration::class,
            TransactionAutoConfiguration::class,
            ValidationAutoConfiguration::class
        ]
)
class Application {

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            // spring-boot
            // https://github.com/zhudyos/duic/issues/17
            // 搜索 /etc/duic 目录中的配置文件
            runApplication<Application>("--spring.config.name=duic,application", "--spring.config.additional-location=/etc/duic/") {
                setBanner(DuicBanner())

                val usable = ApplicationListener<ApplicationUsableEvent> {
                    println("""
====================================================================================
//                           duic start successful                                //
====================================================================================
""")
                }


                val unusable = ApplicationListener<ApplicationUnusableEvent> { event ->
                    SpringApplication.exit(event.applicationContext, ExitCodeGenerator { 1 })
                }

                addListeners(usable, unusable)
            }
        }

    }
}