package logic

trait EventListener

trait KeyListener extends EventListener {
  def onKeyPress(key: Int, action: Int): Unit
}

trait CursorListener extends EventListener {
  def onCursorMove(difference: (Float, Float)): Unit
}
