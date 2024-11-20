package com.github.synnerz.akutz.api.objects.keybind

import com.github.synnerz.akutz.api.objects.state.StateVar
import net.minecraft.client.Minecraft
import net.minecraft.client.resources.I18n
import net.minecraft.client.settings.KeyBinding
import net.minecraftforge.fml.client.registry.ClientRegistry
import org.apache.commons.lang3.ArrayUtils

/**
 * Taken from ChatTriggers under MIT License
 * [Link](https://github.com/ChatTriggers/ChatTriggers/blob/master/src/main/kotlin/com/chattriggers/ctjs/minecraft/objects/keybind/KeyBind.kt)
 */
class Keybind : StateVar<Boolean> {
    private val keyBinding: KeyBinding

    init {
        KeybindHandler.registerKeybind(this)
    }

    @JvmOverloads
    constructor(description: String, keyCode: Int, category: String = "Akutz") : super(false) {
        val possibleDuplicate = Minecraft.getMinecraft().gameSettings.keyBindings.find {
            I18n.format(it.keyDescription) == I18n.format(description) &&
                    I18n.format(it.keyCategory) == I18n.format(category)
        }

        if (possibleDuplicate != null) {
            require(possibleDuplicate in customKeyBindings) {
                "KeyBind already exists! To get a KeyBind from an existing Minecraft KeyBinding, " +
                        "use the other Keybind constructor."
            }
            keyBinding = possibleDuplicate
        } else {
            if (category !in KeyBinding.getKeybinds()) {
                uniqueCategories[category] = 0
            }
            uniqueCategories[category] = uniqueCategories[category]!! + 1
            keyBinding = KeyBinding(description, keyCode, category)
            ClientRegistry.registerKeyBinding(keyBinding)
            customKeyBindings.add(keyBinding)
        }
    }

    constructor(keyBinding: KeyBinding) : super(false) {
        this.keyBinding = keyBinding
    }

    internal fun onTick() {
        set(keyBinding.isKeyDown)
    }

    fun getDescription(): String = keyBinding.keyDescription
    fun getKeyCode(): Int = keyBinding.keyCode
    fun getCategory(): String = keyBinding.keyCategory

    companion object {
        private val customKeyBindings = mutableListOf<KeyBinding>()
        private val uniqueCategories = mutableMapOf<String, Int>()

        private fun removeKeyBinding(keyBinding: KeyBinding) {
            Minecraft.getMinecraft().gameSettings.keyBindings = ArrayUtils.removeElement(
                Minecraft.getMinecraft().gameSettings.keyBindings,
                keyBinding
            )
            val category = keyBinding.keyCategory

            if (category in uniqueCategories) {
                uniqueCategories[category] = uniqueCategories[category]!! - 1

                if (uniqueCategories[category] == 0) {
                    uniqueCategories.remove(category)
                    KeyBinding.getKeybinds().remove(category)
                }
            }
        }

        @JvmStatic
        fun removeKeyBind(keyBind: Keybind) {
            val keyBinding = keyBind.keyBinding
            if (keyBinding !in customKeyBindings) return

            removeKeyBinding(keyBinding)
            customKeyBindings.remove(keyBinding)
            KeybindHandler.unregisterKeybind(keyBind)
        }

        @JvmStatic
        fun clearKeyBinds() {
            val copy = KeybindHandler.getKeybinds().toList()
            copy.forEach(::removeKeyBind)
            customKeyBindings.clear()
            KeybindHandler.clearKeybinds()
        }
    }
}