package logic

class World(val addEventListener: EventListener => Unit) {
  val player = new Player(2.5f, 0f, 2.5f)
  val stage = new Stage()

  def tick(): Unit = {
    player.tick(this)
  }

  addEventListener(player)
}
