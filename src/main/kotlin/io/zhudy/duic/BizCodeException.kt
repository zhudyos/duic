/**
 * Copyright 2017-2018 the original author or authors
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
package io.zhudy.duic

/**
 * 业务异常, 与[BizCode]联合使用.
 *
 * 该异常类型重写了[fillInStackTrace]方法, 不会记录详细的异常堆栈信息.
 *
 * @author Kevin Zou <kevinz@weghst.com>
 */
class BizCodeException : RuntimeException {

    val bizCode: BizCode

    /**
     * 构建一个带有业务码的业务异常.
     */
    constructor(bizCode: BizCode) : super() {
        this.bizCode = bizCode
    }

    /**
     * 构建一个业务码与自定义描述的业务异常.
     */
    constructor(bizCode: BizCode, message: String) : super(message) {
        this.bizCode = bizCode
    }

    /**
     * 根据目标异常构建业务码异常.
     */
    constructor(bizCode: BizCode, e: Exception) : super(e) {
        this.bizCode = bizCode
    }

    /**
     * 不记录堆栈详细信息.
     */
    override fun fillInStackTrace() = this

    /**
     * 重写 message 方法, 在打印日志是会调用该方法打印日志信息.
     */
    override val message: String
        get() {
            var m = """{"code": ${bizCode.code}, "message": "${bizCode.msg}"}"""
            if (!super.message.isNullOrEmpty()) {
                m += ", message: ${super.message}"
            }
            if (cause != null) {
                m += ", cause message: ${cause.message}"
            }
            return m
        }

    /**
     * 确切的消息.
     */
    val exactMessage: String
        get() = super.message ?: ""

    override fun toString(): String {
        return BizCodeException::class.qualifiedName + "${BizCodeException::class.qualifiedName}: $message"
    }
}
