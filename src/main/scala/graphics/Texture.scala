package graphics

import de.matthiasmann.twl.utils.PNGDecoder
import graphics.Utils.glCheck
import org.lwjgl.opengl.GL11.{
  GL_NEAREST,
  GL_RGBA,
  GL_TEXTURE_2D,
  GL_TEXTURE_MAG_FILTER,
  GL_TEXTURE_MIN_FILTER,
  GL_UNSIGNED_BYTE,
  glBindTexture,
  glGenTextures,
  glTexImage2D,
  glTexParameteri,
}

import java.nio.ByteBuffer

class Texture(val path: String) {
  private val textureHandle: Int = loadTexture()
  private def loadTexture(): Int = {
    // Load and decode image
    val imageStream = getClass.getResourceAsStream(path)
    val decoder = PNGDecoder(imageStream)
    val imageBuffer = ByteBuffer.allocateDirect(4 * decoder.getWidth * decoder.getHeight)
    decoder.decode(imageBuffer, 4 * decoder.getWidth, PNGDecoder.Format.RGBA)
    imageBuffer.flip()

    // Create texture object
    val handle = glCheck { glGenTextures() }
    glCheck { glBindTexture(GL_TEXTURE_2D, handle) }
    glCheck {
      glTexImage2D(
        GL_TEXTURE_2D,
        0,
        GL_RGBA,
        decoder.getWidth,
        decoder.getHeight,
        0,
        GL_RGBA,
        GL_UNSIGNED_BYTE,
        imageBuffer,
      )
    }
    glCheck { glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST) }
    glCheck { glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST) }
    imageStream.close()
    handle
  }

  def bind(): Unit = {
    glCheck { glBindTexture(GL_TEXTURE_2D, textureHandle) }
  }

  def unbind(): Unit = {
    glCheck { glBindTexture(GL_TEXTURE_2D, 0) }
  }
}
