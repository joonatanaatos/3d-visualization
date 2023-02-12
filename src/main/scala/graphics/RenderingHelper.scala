package graphics

import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.{
  GL_BLEND,
  GL_COLOR_BUFFER_BIT,
  GL_DEPTH_BUFFER_BIT,
  GL_FLOAT,
  GL_SRC_ALPHA,
  GL_SRC_COLOR,
  GL_TRIANGLE_STRIP,
  glBlendFunc,
  glClear,
  glClearColor,
  glDrawArrays,
  glEnable,
}
import org.lwjgl.opengl.GL15.{
  GL_ARRAY_BUFFER,
  GL_STATIC_DRAW,
  glBindBuffer,
  glBufferData,
  glGenBuffers,
}
import org.lwjgl.opengl.GL20.{
  glDisableVertexAttribArray,
  glEnableVertexAttribArray,
  glVertexAttribPointer,
}
import org.lwjgl.opengl.GL30.{glBindVertexArray, glGenVertexArrays}
import org.lwjgl.system.MemoryUtil

/**
 * RenderingHelper handles most low-level OpenGL code
 */
class RenderingHelper(val window: Window) {
  // x, y and z coordinate
  private val coordsPerVertex = 3

  // 4 bytes per coordinate
  private val vertexStride = coordsPerVertex * 4

  private val quadrilateralVertices = Array[Float](
    -1f, 1f, 0f, // Top left
    1f, 1f, 0f, // Top right
    -1f, -1f, 0f, // Bottom left
    1f, -1f, 0f, // Bottom right
  ).map(_ / 2)

  this.init()

  private val quadrilateralShaderProgram = new ShaderProgram("quadrilateral")

  private val (quadrilateralVaoHandle, quadrilateralVboHandle) =
    this.createQuadrilateralVertices()

  private def init() = {
    // Create GL capabilities
    glCheck { GL.createCapabilities() }

    glCheck { glEnable(GL_BLEND) }
    glCheck { glBlendFunc(GL_SRC_ALPHA, GL_SRC_COLOR) }

    // Set clear color to black
    glCheck { glClearColor(0f, 0f, 0f, 0f) }
  }

  private def createQuadrilateralVertices(): (Int, Int) = {
    // Allocate memory for quadrilateral vertices
    val vertexBuffer = glCheck { MemoryUtil.memAllocFloat(quadrilateralVertices.length) }
    glCheck { vertexBuffer.put(quadrilateralVertices).flip() }
    // Create and bind VAO
    val vaoHandle = glCheck { glGenVertexArrays() }
    glCheck { glBindVertexArray(vaoHandle) }
    // Create and bind VBO
    val vboHandle = glCheck { glGenBuffers() }
    glCheck { glBindBuffer(GL_ARRAY_BUFFER, vboHandle) }
    // Load data to VBO
    glCheck { glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW) }
    glCheck { MemoryUtil.memFree(vertexBuffer) }
    // Define the structure of the data and store it in one of the attribute lists of the VAO
    glCheck { glVertexAttribPointer(0, coordsPerVertex, GL_FLOAT, false, vertexStride, 0) }
    // Unbind VBO and IBO
    glCheck { glBindBuffer(GL_ARRAY_BUFFER, 0) }
    // Unbind VAO
    glCheck { glBindVertexArray(0) }
    (vaoHandle, vboHandle)
  }

  def drawQuadrilateral(): Unit = {
    // Bind correct VAO and shader program
    glCheck { quadrilateralShaderProgram.bind() }
    glCheck { glBindVertexArray(quadrilateralVaoHandle) }
    glCheck { glEnableVertexAttribArray(0) }

    // Set uniforms here

    // Draw vertices
    glCheck { glDrawArrays(GL_TRIANGLE_STRIP, 0, 4) }

    // Unbind everything and restore state
    glCheck { glDisableVertexAttribArray(0) }
    glCheck { glBindVertexArray(0) }
    glCheck { quadrilateralShaderProgram.unbind() }
  }

  def clear() = {
    // Clear the frame buffer
    glCheck { glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT) }
  }
}
