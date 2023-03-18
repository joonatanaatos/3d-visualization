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
  GL_RENDERER,
  GL_SRC_ALPHA,
  GL_TRIANGLES,
  GL_TRIANGLE_STRIP,
  GL_VERSION,
  glBlendFunc,
  glClear,
  glClearColor,
  glDepthFunc,
  glDrawArrays,
  glEnable,
  glGetString,
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
  glUniform1f,
  glUniform1i,
  glUniform2fv,
  glUniform3fv,
  glUniform4fv,
  glUniformMatrix4fv,
  glVertexAttribPointer,
}
import org.lwjgl.opengl.GL30.{glBindVertexArray, glDeleteVertexArrays, glGenVertexArrays}
import org.lwjgl.system.MemoryUtil
import graphics.Utils.glCheck
import org.joml.{Matrix4f, Vector2f, Vector3f, Vector4f}
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

  val maxNumberOfLights: Int = 10
  private var pointLights: Array[(Vector3f, Float)] = Array[(Vector3f, Float)]()
  private var ambientLightBrightness = 0f

  this.init()
  this.logOpenGLState()

  private val quadrilateralShaderProgram =
    ShaderProgram("quadrilateral", Array("mvpMatrix", "color"))

  private val imageShaderProgram =
    ShaderProgram("image", Array("modelMatrix", "texture", "invertColor"))

  private val textureShaderProgram =
    ShaderProgram(
      "texture",
      Array(
        "mvMatrix",
        "pMatrix",
        "translate",
        "texture",
        "normal",
        "ambientLightBrightness",
      ),
    )

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

  private def logOpenGLState(): Unit = {
    println(s"""
         |OpenGL initialized:
         | - Version: ${glCheck { glGetString(GL_VERSION) }}
         | - Renderer: ${glCheck { glGetString(GL_RENDERER) }}
         |""".stripMargin)
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

  private def matrixToArray(matrix: Matrix4f): Array[Float] = {
    val matrixArray = Array.fill[Float](16)(0)
    matrix.get(matrixArray)
    matrixArray
  }

  private def vectorToArray(vector: Vector3f): Array[Float] = {
    Array(vector.x, vector.y, vector.z)
  }

  private def createViewMatrix(viewDirection: (Float, Float)): Matrix4f = {
    Matrix4f().rotateX(viewDirection(1)).rotateY(viewDirection(0))
  }

  private def createProjectionMatrix(): Matrix4f = {
    Matrix4f().setPerspective(
      math.Pi.toFloat / 3f,
      window.getAspectRatio,
      0.1f,
      Float.PositiveInfinity,
    )
  }

  private def createMvpMatrix(
      modelMatrix: Matrix4f,
      viewDirection: (Float, Float),
  ): Array[Float] = {
    val mvpMatrix = Matrix4f()
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

  /**
   * Draws a textured quadrilateral and applies a 3D transformation.
   * @param modelMatrix
   *   model matrix
   * @param viewDirection
   *   view direction
   * @param texture
   *   texture object
   * @param normal
   *   normal vector
   */
  def drawTexture(
      modelMatrix: Matrix4f = Matrix4f(),
      viewDirection: (Float, Float) = (0f, 0f),
      texture: Texture,
      normal: Vector3f,
      translate: (Float, Float) = (0f, 0f),
  ): Unit = {
    // Bind correct VAO and shader program
    glCheck { textureShaderProgram.bind() }
    glCheck { glBindVertexArray(quadrilateralVaoHandle) }
    glCheck { glEnableVertexAttribArray(vertexPosIndex) }
    glCheck { glEnableVertexAttribArray(textureCoordIndex) }

    // Create MVP matrix
    val projectionMatrix = createProjectionMatrix()
    val viewMatrix = createViewMatrix(viewDirection)

    def applyViewMatrix(vector: Vector3f): Vector3f = {
      val result = Vector4f(vector, 1f).mul(viewMatrix)
      Vector3f(result.x, result.y, result.z)
    }

    val lightPositions = pointLights.map(light => applyViewMatrix(light(0)))

    // Set uniforms
    glCheck {
      glUniformMatrix4fv(
        textureShaderProgram.uniform("mvMatrix"),
        false,
        matrixToArray(Matrix4f(viewMatrix).mul(modelMatrix)),
      )
    }
    glCheck {
      glUniformMatrix4fv(
        textureShaderProgram.uniform("pMatrix"),
        false,
        matrixToArray(projectionMatrix),
      )
    }
    glCheck {
      glUniform3fv(
        textureShaderProgram.uniform("normal"),
        vectorToArray(applyViewMatrix(normal)),
      )
    }
    glCheck {
      glUniform1i(textureShaderProgram.uniform("texture"), 0)
    }
    glCheck {
      glUniform2fv(textureShaderProgram.uniform("translate"), Array(translate(0), translate(1)))
    }
    glCheck {
      glUniform1f(textureShaderProgram.uniform("ambientLightBrightness"), ambientLightBrightness)
    }
    for (i <- 0 until math.min(pointLights.length, maxNumberOfLights)) {
      glCheck {
        glUniform3fv(
          textureShaderProgram.dynamicUniform(s"pointLights[$i].position"),
          vectorToArray(lightPositions(i)),
        )
      }
      glCheck {
        glUniform1f(
          textureShaderProgram.dynamicUniform(s"pointLights[$i].brightness"),
          pointLights(i)(1),
        )
      }
    }

    // Bind texture
    texture.bind()

    // Draw vertices
    glCheck { glDrawArrays(GL_TRIANGLE_STRIP, 0, 4) }

    // Unbind texture
    texture.unbind()

    // Unbind everything and restore state
    glCheck { glDisableVertexAttribArray(vertexPosIndex) }
    glCheck { glDisableVertexAttribArray(textureCoordIndex) }
    glCheck { glBindVertexArray(0) }
    glCheck { textureShaderProgram.unbind() }
  }

  /**
   * Draws a textured quadrilateral without applying a 3D transformation.
   * @param modelMatrix
   *   model matrix
   * @param texture
   *   texture object
   */
  def drawImage(
      modelMatrix: Matrix4f = Matrix4f(),
      texture: Texture,
  ): Unit = {
    // Bind correct VAO and shader program
    glCheck { imageShaderProgram.bind() }
    glCheck { glBindVertexArray(quadrilateralVaoHandle) }
    glCheck { glEnableVertexAttribArray(vertexPosIndex) }
    glCheck { glEnableVertexAttribArray(textureCoordIndex) }

    modelMatrix.translate(0f, 0f, -0.5f)

    glCheck {
      glUniformMatrix4fv(
        imageShaderProgram.uniform("modelMatrix"),
        false,
        matrixToArray(modelMatrix),
      )
    }

    glCheck {
      glUniform1i(imageShaderProgram.uniform("invertColor"), 1)
    }

    glCheck {
      glUniform1i(imageShaderProgram.uniform("texture"), 0)
    }

    // Bind texture
    texture.bind()

    // Draw vertices
    glCheck { glDrawArrays(GL_TRIANGLE_STRIP, 0, 4) }

    // Unbind texture
    texture.unbind()

    // Unbind everything and restore state
    glCheck { glDisableVertexAttribArray(vertexPosIndex) }
    glCheck { glDisableVertexAttribArray(textureCoordIndex) }
    glCheck { glBindVertexArray(0) }
    glCheck { imageShaderProgram.unbind() }
  }

  def clear(): Unit = {
    // Clear the frame buffer
    glCheck { glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT) }
  }

  def setAmbientLightBrightness(brightness: Float): Unit = ambientLightBrightness = brightness

  def setPointLights(lights: Array[(Vector3f, Float)]): Unit = {
    if lights.length > maxNumberOfLights then {
      throw RuntimeException(s"Can't set more than $maxNumberOfLights lights")
    }
    pointLights = lights
  }

  def destroy(): Unit = {
    quadrilateralShaderProgram.destroy()
    glCheck { glDeleteVertexArrays(Array(quadrilateralVaoHandle)) }
    glCheck { glDeleteBuffers(Array(quadrilateralVboHandle)) }
  }
}
