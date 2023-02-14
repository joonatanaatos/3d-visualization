package logic

class World(val addEventListener: (EventListener) => Unit) {
  val player = new Player(0f, 0f, 0f)
  val stage = new Stage()

  def tick() = {
    player.tick()
  }

  addEventListener(player)
}
