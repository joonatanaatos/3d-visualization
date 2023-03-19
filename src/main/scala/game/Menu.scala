package game

import logic.KeyListener
import org.lwjgl.glfw.GLFW

class Menu(game: Game) extends KeyListener {
  val fadeInTime: Int = 300
  var fadeInTimer: Int = 0
  var startTextFaze: Float = 0f
  private val startTextSpeed: Float = 0.02f

  def update(): Unit = {
    if (fadeInTimer < fadeInTime) {
      fadeInTimer += 1
    } else {
      startTextFaze = (startTextFaze + startTextSpeed) % (math.Pi.toFloat * 2f)
    }
  }

  def reset(): Unit = {
    fadeInTimer = 0
    startTextFaze = 0f
  }

  override def onKeyPress(key: Int, action: Int): Unit = {
    if (fadeInTimer == fadeInTime && key == GLFW.GLFW_KEY_SPACE && action == GLFW.GLFW_PRESS) {
      game.startGame()
      startTextFaze = 0f
    }
  }
}
