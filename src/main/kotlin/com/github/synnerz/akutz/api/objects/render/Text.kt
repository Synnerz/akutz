package com.github.synnerz.akutz.api.objects.render

interface Text {
    fun getText(): String
    fun setText(v: String): Text
    fun getWidth(): Float
    fun getHeight(): Float
    fun getVisibleWidth(): Float
    fun getVisibleHeight(): Float
}