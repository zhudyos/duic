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
 * 错误码定义枚举。
 *
 * @author Kevin Zou (kevinz@weghst.com)
 */
enum class BizCodes(override val code: Int, override val msg: String, override val status: Int = 400) : BizCode {

    // 应用错误码
    C_1000(1000, "未找到应用"),
    C_1001(1001, "未找到应用 profile"),
    C_1002(1001, "未找到配置项"),
    C_1003(1003, "修改 app content 失败"),
    C_1004(1004, "修改 app content 失败, 数据版本不一致"),
    C_1005(1005, "数据类型不一致无法合并"),
    C_1006(1006, "app content 不是有效的 yaml 格式"),

    // 用户错误码
    C_2000(2000, "用户不存在"),
    C_2001(2001, "密码不匹配"),
    C_2002(2002, "ROOT 用户禁止删除", 403),
    C_2003(2003, "ROOT 用户禁止重置密码", 403)
    ;
}