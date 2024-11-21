package com.github.synnerz.akutz.api.objects.sound

import com.github.synnerz.akutz.Akutz
import com.github.synnerz.akutz.api.wrappers.Client
import com.github.synnerz.akutz.api.wrappers.Player
import com.github.synnerz.akutz.api.wrappers.World
import com.github.synnerz.akutz.mixin.AccessorSoundHandler
import net.minecraft.client.audio.SoundCategory
import net.minecraft.client.audio.SoundManager
import net.minecraftforge.fml.relauncher.ReflectionHelper
import paulscode.sound.SoundSystem
import java.io.File
import java.net.MalformedURLException

/**
 * Taken from ChatTriggers under MIT License
 * [Link](https://github.com/ChatTriggers/ChatTriggers/blob/master/src/main/kotlin/com/chattriggers/ctjs/minecraft/objects/Sound.kt)
 */
class Sound(
    private val config: HashMap<String, Any>
) {
    private var soundSystem: SoundSystem? = null
    private var source: String
    var shouldListen: Boolean = false

    init {
        val realSource = config["source"] ?: throw IllegalArgumentException("Sound source \"${config["source"]}\" is not a valid source.")
        this.source = realSource as String

        if (World.isLoaded()) {
            loadSoundSystem()
            try {
                bootstrap()
            } catch (e: MalformedURLException) {
                e.printStackTrace()
            }
        } else shouldListen = true

        sounds.add(this)
    }

    fun onWorldLoad() {
        shouldListen = false

        loadSoundSystem()
        try {
            bootstrap()
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        }
    }

    private fun loadSoundSystem() {
        val soundManager = (Client.getMinecraft().soundHandler as AccessorSoundHandler).soundManager
        soundSystem = ReflectionHelper.getPrivateValue<SoundSystem, SoundManager>(
            SoundManager::class.java,
            soundManager,
            "sndSystem",
            "field_148620_e"
        )
    }

    @Throws(MalformedURLException::class)
    private fun bootstrap() {
        val priority = config.getOrDefault("priority", false) as Boolean
        val loop = config.getOrDefault("loop", false) as Boolean
        val stream = config.getOrDefault("stream", false) as Boolean

        val file = File(Akutz.configLocation, source)
        if (!file.exists()) throw IllegalArgumentException("The file \"$source\" does not exist.")

        val url = file.toURI().toURL()
        val x = (config.getOrDefault("x", Player.getX()) as Number).toFloat()
        val y = (config.getOrDefault("y", Player.getY()) as Number).toFloat()
        val z = (config.getOrDefault("z", Player.getZ()) as Number).toFloat()
        val attModel = (config.getOrDefault("attenuation", 1) as Number).toInt()
        val distOrRoll = 16

        if (stream) {
            soundSystem!!.newStreamingSource(
                priority,
                source,
                url,
                source,
                loop,
                x,
                y,
                z,
                attModel,
                distOrRoll.toFloat()
            )
        } else {
            soundSystem!!.newSource(
                priority,
                source,
                url,
                source,
                loop,
                x,
                y,
                z,
                attModel,
                distOrRoll.toFloat()
            )
        }

        if (config["volume"] != null) {
            setVolume((config["volume"] as Number).toFloat())
        }

        if (config["pitch"] != null) {
            setPitch((config["pitch"] as Number).toFloat())
        }

        if (config["category"] != null) {
            setCategory(config["category"] as String)
        }
    }

    fun play() {
        soundSystem!!.play(source)
    }

    fun pause() {
        soundSystem!!.pause(source)
    }

    fun stop() {
        soundSystem!!.stop(source)
    }

    fun rewind() {
        soundSystem!!.rewind(source)
    }

    fun setAttenuation(model: Int) = apply {
        soundSystem!!.setAttenuation(source, model)
    }

    fun getPitch() = soundSystem!!.getPitch(source)

    fun setPitch(pitch: Float) = apply {
        soundSystem!!.setPitch(source, pitch)
    }

    fun setPosition(x: Float, y: Float, z: Float) = apply {
        soundSystem!!.setPosition(source, x, y, z)
    }

    fun getVolume() = soundSystem!!.getVolume(source)

    fun setVolume(vol: Float) = apply {
        soundSystem!!.setVolume(source, vol)
    }

    fun setCategory(category: String) = apply {
        val cate = SoundCategory.getCategory(category)
        setVolume(Client.getMinecraft().gameSettings.getSoundLevel(cate))
    }

    companion object {
        @JvmStatic
        val sounds = mutableSetOf<Sound>()
    }
}