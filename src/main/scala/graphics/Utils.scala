package graphics

import org.lwjgl.opengl.GL11.{GL_NO_ERROR, glGetError}

object Utils {
  inline def glCheck[T](inline op: T): T = {
    val result = op
    val error = glGetError()
    if error != GL_NO_ERROR then {
      throw new RuntimeException(s"OpenGL error: $error")
    }
    result
  }
}
