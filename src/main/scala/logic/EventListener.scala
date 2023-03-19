package logic

import org.joml.Vector2f

trait EventListener

trait KeyListener extends EventListener {
  def onKeyPress(key: Int, action: Int): Unit
}

trait CursorListener extends EventListener {
  def onCursorMove(difference: Vector2f): Unit
}
