package graphics

import org.lwjgl.opengl.GL11.{GL_NO_ERROR, glGetError}

/**
 * Object for containing many utility functions
 */
object Utils {

  /**
   * Checks for OpenGL errors and throws an exception when necessary
   *
   * @param op
   *   Function that is to be run before checking for GL errors
   * @tparam T
   *   Return type of the given function
   * @return
   *   The return value of the given function
   * @throws RuntimeException
   *   When an OpenGL error has occured
   */
  inline def glCheck[T](inline op: T): T = {
    val result = op
    val error = glGetError()
    if error != GL_NO_ERROR then {
      throw new RuntimeException(s"OpenGL error: $error")
    }
    result
  }
}
