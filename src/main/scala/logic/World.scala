package logic

import org.lwjgl.glfw.GLFWKeyCallbackI

class World(val addKeyListener: GLFWKeyCallbackI => Unit) {
  val player = new Player(0f, 0f, 0f)
  val stage = new Stage()

  def tick() = {
    player.tick()
  }

  addKeyListener(player.onKeyInput)
}
