package com.github.synnerz.akutz.hooks

import com.github.synnerz.akutz.api.events.EventType

object CrashReportHook {
    fun trigger() {
        EventType.Crashed.triggerAll()
    }
}