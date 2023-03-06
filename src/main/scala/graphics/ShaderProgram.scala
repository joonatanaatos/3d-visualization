package graphics

import graphics.Utils.glCheck
import org.lwjgl.opengl.GL20.{
  GL_COMPILE_STATUS,
  GL_FRAGMENT_SHADER,
  GL_LINK_STATUS,
  GL_VALIDATE_STATUS,
  GL_VERTEX_SHADER,
  glAttachShader,
  glCompileShader,
  glCreateProgram,
  glCreateShader,
  glDeleteProgram,
  glDetachShader,
  glGetProgramInfoLog,
  glGetProgrami,
  glGetShaderInfoLog,
  glGetShaderi,
  glGetUniformLocation,
  glLinkProgram,
  glShaderSource,
  glUseProgram,
  glValidateProgram,
}

import java.nio.file.{Files, Paths}
import scala.collection.immutable

/**
 * ShaderProgram represents an OpenGL shader program. It loads the shader file with the given name
 * from the /glsl directory and then compiles the shader. The ShaderProgram class also provides an
 * interface for setting uniform variables but not vertex attributes.
 *
 * @param shaderName
 *   Name of the shader
 * @param uniformNames
 *   List of uniforms used in the shader
 */
class ShaderProgram(val shaderName: String, val uniformNames: Array[String]) {
  private val programHandle = glCheck { glCreateProgram() }
  if programHandle == 0 then {
    throw new RuntimeException(s"Failed to create program for \"$shaderName\"")
  }

  private val vertexShaderCode = Files.readString(Paths.get(s"src/main/glsl/$shaderName.vert"))
  private val fragmentShaderCode = Files.readString(Paths.get(s"src/main/glsl/$shaderName.frag"))

  private val vertexShaderHandle = createShader(vertexShaderCode, GL_VERTEX_SHADER)
  private val fragmentShaderHandle = createShader(fragmentShaderCode, GL_FRAGMENT_SHADER)

  this.link()

  private val uniformHandles = findUniformHandles()

  private def createShader(shaderCode: String, shaderType: Int): Int = {
    // Create shader
    val shaderHandle = glCheck { glCreateShader(shaderType) }
    if shaderHandle == 0 then {
      throw new RuntimeException(s"Failed to create shader for \"$shaderName\"")
    }
    // Load and compile shader code
    glCheck { glShaderSource(shaderHandle, shaderCode) }
    glCheck { glCompileShader(shaderHandle) }
    // Check for errors
    if glGetShaderi(shaderHandle, GL_COMPILE_STATUS) == 0 then {
      throw new RuntimeException(
        s"Failed to compile shader code:\n${glGetShaderInfoLog(shaderHandle, 1024)}",
      )
    }
    // Attach shader to program
    glCheck { glAttachShader(programHandle, shaderHandle) }
    shaderHandle
  }

  private def link(): Unit = {
    glCheck { glLinkProgram(programHandle) }
    // Check for errors
    if glGetProgrami(programHandle, GL_LINK_STATUS) == 0 then {
      throw new Exception(
        s"Failed to link shader program:\n${glGetProgramInfoLog(programHandle, 1024)}",
      )
    }
    // Detach shaders
    if vertexShaderHandle != 0 then {
      glCheck {
        glDetachShader(programHandle, vertexShaderHandle)
      }
    }
    if fragmentShaderHandle != 0 then {
      glCheck {
        glDetachShader(programHandle, fragmentShaderHandle)
      }
    }
    // Validate program
    glCheck { glValidateProgram(programHandle) }
    // Check for errors
    if glGetProgrami(programHandle, GL_VALIDATE_STATUS) == 0 then {
      throw new Exception(
        s"Failed to link shader program:\n${glGetProgramInfoLog(programHandle, 1024)}",
      )
    }
  }

  private def findUniformHandles(): immutable.Map[String, Int] = {
    uniformNames.map(name => (name, glCheck { glGetUniformLocation(programHandle, name) })).toMap
  }

  def dynamicUniform(name: String): Int = {
    glCheck { glGetUniformLocation(programHandle, name) }
  }

  def uniform(name: String): Int = {
    uniformHandles(name)
  }

  def bind(): Unit = {
    glCheck { glUseProgram(programHandle) }
  }

  def unbind(): Unit = {
    glCheck { glUseProgram(0) }
  }

  def destroy(): Unit = {
    this.unbind()
    if programHandle != 0 then {
      glCheck { glDeleteProgram(programHandle) }
    }
  }
}
