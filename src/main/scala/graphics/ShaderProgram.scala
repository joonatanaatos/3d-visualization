package graphics

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
  glLinkProgram,
  glShaderSource,
  glUseProgram,
  glValidateProgram,
}

import java.nio.file.{Files, Paths}

class ShaderProgram(val shaderName: String) {
  private val programHandle = glCheck { glCreateProgram() }
  if programHandle == 0 then {
    throw new RuntimeException(s"Failed to create program for \"$shaderName\"")
  }

  private val vertexShaderCode = Files.readString(Paths.get(s"src/main/glsl/$shaderName.vert"))
  private val fragmentShaderCode = Files.readString(Paths.get(s"src/main/glsl/$shaderName.frag"))

  private var vertexShaderHandle = createShader(vertexShaderCode, GL_VERTEX_SHADER)
  private var fragmentShaderHandle = createShader(fragmentShaderCode, GL_FRAGMENT_SHADER)

  this.link()

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
