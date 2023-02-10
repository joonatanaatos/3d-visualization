package graphics

import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*

/**
 * RenderingHelper handles most low-level OpenGL code
 */
class RenderingHelper(val window: Window) {

  init()

  private def init() = {
    // Create GL capabilities
    GL.createCapabilities()
    // Set clear color to black
    glClearColor(0f, 0f, 0f, 0f)
  }

  def clear() = {
    // Clear the frame buffer
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT)
  }
}
