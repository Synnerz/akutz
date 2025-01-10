package com.github.synnerz.akutz.engine.impl.custom.event

import io.vertx.core.VertxOptions

class EventLoopOptions(
    var vopts: VertxOptions
) {
    constructor() : this(VertxOptions().setWorkerPoolSize(DEFAULT_THREAD_POOL_SIZE))
    var gcBeforeClose: Boolean = true
    var pooled: Boolean = false

    companion object {
        const val DEFAULT_THREAD_POOL_SIZE = 4
    }
}
