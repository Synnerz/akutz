package com.github.synnerz.akutz.api.objects.render

import com.github.synnerz.akutz.api.libs.render.Renderer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.texture.DynamicTexture
import java.awt.image.BufferedImage
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.Charset
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.imageio.ImageIO

class Image(
    private var img: BufferedImage?,
    private var width: Int = img!!.width,
    private var height: Int = img!!.height
) {
    private var texture: DynamicTexture? = null
    private var needBake = true

    fun getWidth() = width
    fun getTextureWidth() = width
    fun getHeight() = height
    fun getTextureHeight() = height

    private fun bake() {
        needBake = false
        if (img == null) return
        if (texture != null) {
            if (width == img!!.width && height == img!!.height) {
                img!!.getRGB(0, 0, width, height, texture!!.textureData, 0, width)
                img = null
                return
            }
            width = img!!.width
            height = img!!.height
            texture!!.deleteGlTexture()
        }
        texture = DynamicTexture(img)
        img = null

        IMAGES.add(this)
    }

    fun update(image: BufferedImage) = apply {
        img = image
        needBake = true
    }

    fun getTexture(): DynamicTexture? {
        if (needBake) bake()

        return texture
    }

    fun destroy() {
        texture?.deleteGlTexture()
        texture = null
        img = null
        IMAGES.remove(this)
    }

    @JvmOverloads
    fun draw(
        x: Double, y: Double,
        width: Double = getWidth().toDouble(),
        height: Double = width / getWidth() * getHeight()
    ) = apply {
        if (needBake) bake()
        if (texture == null) return@apply

        GlStateManager.enableTexture2D()
        GlStateManager.bindTexture(texture!!.glTextureId)
        Renderer.drawTexturedRect(x, y, width, height)
    }

    companion object {
        @JvmStatic
        val IMAGES = mutableSetOf<Image>()

        @JvmStatic
        fun fromFile(file: String) = fromFile(File(file))

        @JvmStatic
        fun fromFile(file: File) = Image(ImageIO.read(file))

        @JvmStatic
        fun fromUrl(url: String): Image = fromUrl(URL(url))

        @JvmStatic
        private val REG1 =
            Pattern.compile("<meta property=\"(?:og:image|twitter:image)\" content=\"(?<url>.+?)\".*?/?>")

        @JvmStatic
        private val REG2 = Pattern.compile("<img.*?src=\"(?<url>.+?)\".*?>")

        @JvmStatic
        fun fromUrl(url: URL): Image {
            val conn = url.openConnection() as HttpURLConnection
            try {
                conn.requestMethod = "GET"
                conn.useCaches = true
                conn.instanceFollowRedirects = true
                conn.addRequestProperty(
                    "User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36"
                )
                if (url.host.contains("imgur")) conn.addRequestProperty("Referer", "https://imgur.com/")
                conn.readTimeout = 1000
                conn.connectTimeout = 1000
                conn.inputStream.use { stream ->
                    if (conn.getHeaderField("Content-Type").contains("text/html")) {
                        val body = stream.bufferedReader(Charset.forName("UTF-8")).use { it.readText() }
                        var imgURL = ""
                        var m: Matcher
                        if ((REG1.matcher(body).also { m = it }).find()) {
                            imgURL = m.group(1)
                        } else if ((REG2.matcher(body).also { m = it }).find()) {
                            imgURL = m.group(1)
                        }
                        if (imgURL.startsWith('/')) imgURL = "${url.getProtocol()}://${url.getHost()}${imgURL}"
                        imgURL = imgURL.trim()
                        conn.disconnect()
                        return fromUrl(imgURL)
                    }
                    val img = ImageIO.read(stream)
                    conn.disconnect()
                    return Image(img)
                }
            } finally {
                conn.disconnect()
            }
        }
    }
}