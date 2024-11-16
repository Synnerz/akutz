package com.github.synnerz.akutz.api.events

class NormalTrigger(
    method: (args: Array<out Any?>) -> Unit,
    type: EventType
) : EventTrigger(method, type)