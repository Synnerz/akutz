package com.github.synnerz.akutz.api.events

class NormalEvent(
    method: (args: Array<out Any?>) -> Unit,
    type: EventType
) : BaseEvent(method, type)