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

import io.zhudy.duic.DuicVersion
import org.springframework.boot.Banner
import org.springframework.boot.ansi.AnsiColor
import org.springframework.boot.ansi.AnsiOutput
import org.springframework.boot.ansi.AnsiStyle
import org.springframework.core.env.Environment
import java.io.PrintStream

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
class DuicBanner : Banner {

    private val banner = """ ______              _    ______
|_   _ `.           (_) .' ___  |
  | | `. \ __   _   __ / .'   \_|
  | |  | |[  | | | [  || |
 _| |_.' / | \_/ |, | |\ `.___.'\
|______.'  '.__.'_/[___]`.____ .'"""
    private val duic = " :: duic :: "
    private val lineSize = 34

    override fun printBanner(environment: Environment, sourceClass: Class<*>, out: PrintStream) {
        out.println(banner)

        var version = DuicVersion.version
        if (version.isNotEmpty()) {
            version = " (v$version)"
        }

        val padding = StringBuilder()
        while (padding.length <= lineSize - (version.length + duic.length)) {
            padding.append(" ")
        }

        out.println(AnsiOutput.toString(
                AnsiColor.GREEN, ":: duic ::",
                AnsiColor.DEFAULT, padding,
                AnsiStyle.FAINT, version
        ))
        out.println()
    }
}