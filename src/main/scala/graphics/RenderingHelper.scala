package graphics

import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.{
  GL_BLEND,
  GL_COLOR_BUFFER_BIT,
  GL_DEPTH_BUFFER_BIT,
  GL_DEPTH_TEST,
  GL_FLOAT,
  GL_LEQUAL,
  GL_ONE_MINUS_SRC_ALPHA,
  GL_SRC_ALPHA,
  GL_TRIANGLES,
  GL_TRIANGLE_STRIP,
  glBlendFunc,
  glClear,
  glClearColor,
  glDepthFunc,
  glDrawArrays,
  glEnable,
}
import org.lwjgl.opengl.GL15.{
  GL_ARRAY_BUFFER,
  GL_STATIC_DRAW,
  glBindBuffer,
  glBufferData,
  glDeleteBuffers,
  glGenBuffers,
}
import org.lwjgl.opengl.GL20.{
  glDisableVertexAttribArray,
  glEnableVertexAttribArray,
  glGetAttribLocation,
  glUniform1i,
  glUniform4fv,
  glUniformMatrix4fv,
  glVertexAttribPointer,
}
import org.lwjgl.opengl.GL30.{glBindVertexArray, glDeleteVertexArrays, glGenVertexArrays}
import org.lwjgl.system.MemoryUtil
import graphics.Utils.glCheck
import org.joml.{Matrix4f, Vector2f, Vector3f}
import org.lwjgl.opengl.GL13.GL_MULTISAMPLE

import java.nio.{ByteBuffer, ByteOrder, FloatBuffer}

/**
 * RenderingHelper handles most low-level OpenGL code
 *
 * @param window
 *   Window that is to be rendered onto
 */
class RenderingHelper(val window: Window) {
  // x, y and z coordinate
  private val vertexPosSize = 3
  // s and t coordinate
  private val textureCoordSize = 2
  // 4 bytes per float
  private val vertexStride = (vertexPosSize + textureCoordSize) * 4

  private val vertexPosIndex = 0
  private val textureCoordIndex = 1

  private val quadrilateralVertices = Array[Float](
    -1f, 1f, 0f, 0f, 0f, // Top left
    1f, 1f, 0f, 1f, 0f, // Top right
    -1f, -1f, 0f, 0f, 1f, // Bottom left
    1f, -1f, 0f, 1f, 1f, // Bottom right
  )

  this.init()

  private val quadrilateralShaderProgram =
    new ShaderProgram("quadrilateral", Array("mvpMatrix", "color"))

  private val textureShaderProgram =
    new ShaderProgram("texture", Array("mvpMatrix", "texture"))

  private val (quadrilateralVaoHandle, quadrilateralVboHandle) =
    this.createQuadrilateralVertices()

  private def init(): Unit = {
    // Create GL capabilities
    glCheck { GL.createCapabilities() }

    // Configure OpenGL
    glCheck { glEnable(GL_BLEND) }
    glCheck { glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA) }
    glCheck { glEnable(GL_DEPTH_TEST) }
    glCheck { glDepthFunc(GL_LEQUAL) }
    glCheck { glEnable(GL_MULTISAMPLE) }

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
    glCheck {
      glVertexAttribPointer(vertexPosIndex, vertexPosSize, GL_FLOAT, false, vertexStride, 0)
    }
    // Set texture coordinates
    glCheck {
      glVertexAttribPointer(
        textureCoordIndex,
        textureCoordSize,
        GL_FLOAT,
        false,
        vertexStride,
        4 * vertexPosSize, // Offset
      )
    }
    // Unbind VBO
    glCheck { glBindBuffer(GL_ARRAY_BUFFER, 0) }
    // Unbind VAO
    glCheck { glBindVertexArray(0) }
    (vaoHandle, vboHandle)
  }

  private def createMvpMatrix(
      modelMatrix: Matrix4f,
      viewDirection: (Float, Float),
  ): Array[Float] = {
    val mvpMatrix = new Matrix4f()
    // Perspective
    mvpMatrix.setPerspective(
      math.Pi.toFloat / 3f,
      window.getAspectRatio,
      0.1f,
      Float.PositiveInfinity,
    )
    // View
    mvpMatrix.rotateX(viewDirection(1))
    mvpMatrix.rotateY(viewDirection(0))
    // Model
    mvpMatrix.mul(modelMatrix)
    // Transfer matrix into array
    val mvpMatrixArray = Array.fill[Float](16)(0)
    mvpMatrix.get(mvpMatrixArray)
    mvpMatrixArray
  }

  def drawQuadrilateral(
      modelMatrix: Matrix4f = Matrix4f(),
      viewDirection: (Float, Float) = (0f, 0f),
      color: Array[Float] = Array(1f, 1f, 1f, 1f),
  ): Unit = {
    // Bind correct VAO and shader program
    glCheck { quadrilateralShaderProgram.bind() }
    glCheck { glBindVertexArray(quadrilateralVaoHandle) }
    glCheck { glEnableVertexAttribArray(vertexPosIndex) }

    // Create MVP matrix
    val mvpMatrix = createMvpMatrix(modelMatrix, viewDirection)

    // Set uniforms
    glCheck {
      glUniformMatrix4fv(
        quadrilateralShaderProgram.uniform("mvpMatrix"),
        false,
        mvpMatrix,
      )
    }
    glCheck {
      glUniform4fv(quadrilateralShaderProgram.uniform("color"), color)
    }

    // Draw vertices
    glCheck { glDrawArrays(GL_TRIANGLE_STRIP, 0, 4) }

    // Unbind everything and restore state
    glCheck { glDisableVertexAttribArray(vertexPosIndex) }
    glCheck { glBindVertexArray(0) }
    glCheck { quadrilateralShaderProgram.unbind() }
  }

  def drawImage(
      modelMatrix: Matrix4f = Matrix4f(),
      viewDirection: (Float, Float) = (0f, 0f),
      texture: Texture,
  ): Unit = {
    // Bind correct VAO and shader program
    glCheck { textureShaderProgram.bind() }
    glCheck { glBindVertexArray(quadrilateralVaoHandle) }
    glCheck { glEnableVertexAttribArray(vertexPosIndex) }
    glCheck { glEnableVertexAttribArray(textureCoordIndex) }

    // Create MVP matrix
    val mvpMatrix = createMvpMatrix(modelMatrix, viewDirection)

    // Set uniforms
    glCheck {
      glUniformMatrix4fv(
        textureShaderProgram.uniform("mvpMatrix"),
        false,
        mvpMatrix,
      )
    }
    glCheck {
      glUniform1i(textureShaderProgram.uniform("texture"), 0)
    }

    // Bind texture
    texture.bind()

    // Draw vertices
    glCheck { glDrawArrays(GL_TRIANGLE_STRIP, 0, 4) }

    // Unbind everything and restore state
    texture.unbind()

    glCheck { glDisableVertexAttribArray(vertexPosIndex) }
    glCheck { glDisableVertexAttribArray(textureCoordIndex) }
    glCheck { glBindVertexArray(0) }
    glCheck { textureShaderProgram.unbind() }
  }

  def clear(): Unit = {
    // Clear the frame buffer
    glCheck { glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT) }
  }

  def destroy(): Unit = {
    quadrilateralShaderProgram.destroy()
    glCheck { glDeleteVertexArrays(Array(quadrilateralVaoHandle)) }
    glCheck { glDeleteBuffers(Array(quadrilateralVboHandle)) }
  }
}
