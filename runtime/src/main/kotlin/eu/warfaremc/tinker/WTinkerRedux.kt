/*
 * This file is part of WarfareMC, licensed under the MIT License.
 *
 * Copyright (C) 2021 WarfareMC & Team
 *
 * Permission is hereby granted, free of charge,
 * to any person obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package eu.warfaremc.tinker

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import mu.KotlinLogging
import org.bukkit.plugin.java.JavaPlugin
import java.util.*
import java.util.concurrent.TimeUnit

@PublishedApi
internal lateinit var plugin: WTinkerRedux
    private set

@PublishedApi
internal lateinit var kguava: Cache<Any, Any>
    private set

public class WTinkerRedux : JavaPlugin() {
    init {
        plugin = this
        kguava = CacheBuilder.newBuilder()
            .expireAfterWrite(Long.MAX_VALUE, TimeUnit.DAYS)
            .build()
    }

    private val logger  by lazy { KotlinLogging.logger { }}
    private val session by lazy { UUID.randomUUID().toString() }

    override fun onLoad() {
        if (dataFolder.exists() == false)
            dataFolder.mkdirs().also { logger.info { "[IO] dataFolder /'${dataFolder.path}' created" } }
        saveDefaultConfig()
    }

    override fun onEnable() {
        logger.info { "Setting up internals ..." }
        config.options().copyDefaults(true)
        saveConfig()
    }

}

public fun <T> identity(t: T): T = t