package logic

import org.lwjgl.glfw.{GLFW, GLFWKeyCallbackI}
import org.lwjgl.glfw.GLFW.{
  GLFW_KEY_W,
  GLFW_KEY_S,
  GLFW_KEY_A,
  GLFW_KEY_D,
  GLFW_KEY_UP,
  GLFW_KEY_DOWN,
  GLFW_KEY_LEFT,
  GLFW_KEY_RIGHT,
  GLFW_PRESS,
  GLFW_RELEASE,
}
import scala.collection.mutable.Set

class Player(var xPos: Float, var yPos: Float, var zPos: Float) {
  private var pressedKeys: Set[Int] = Set()
  private val speed = 0.04f

  def tick(): Unit = {
    val (dx, dy, dz) = normalizedVelocity()
    xPos += dx * speed
    yPos += dy * speed
    zPos += dz * speed
  }

  private def normalizedVelocity(): (Float, Float, Float) = {
    var xVel = 0
    var yVel = 0
    var zVel = 0
    if pressedKeys.contains(GLFW_KEY_UP) || pressedKeys.contains(GLFW_KEY_W) then {
      zVel += 1
    }
    if pressedKeys.contains(GLFW_KEY_DOWN) || pressedKeys.contains(GLFW_KEY_S) then {
      zVel -= 1
    }
    if pressedKeys.contains(GLFW_KEY_RIGHT) || pressedKeys.contains(GLFW_KEY_D) then {
      xVel += 1
    }
    if pressedKeys.contains(GLFW_KEY_LEFT) || pressedKeys.contains(GLFW_KEY_A) then {
      xVel -= 1
    }

    val length = math.sqrt(xVel * xVel + yVel * yVel + zVel * zVel).toFloat
    if length == 0 then (0f, 0f, 0f) else (xVel / length, yVel / length, zVel / length)
  }

  val onKeyInput: GLFWKeyCallbackI = (window, key, scancode, action, mods) => {
    action match {
      case GLFW_PRESS => {
        pressedKeys += key
      }
      case GLFW_RELEASE => {
        pressedKeys -= key
      }
      case _ =>
    }
  }
}
