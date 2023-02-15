package logic

class World(val addEventListener: EventListener => Unit) {
  val player = new Player(0f, 0f, 0f)
  val stage = new Stage()

  def tick(): Unit = {
    player.tick()
  }

  addEventListener(player)
}
