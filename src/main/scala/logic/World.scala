package logic

/**
 * World stores and updates the game world's state.
 *
 * @param addEventListener
 *   Function for adding event listeners for user input
 */
class World(val addEventListener: EventListener => Unit) {
  val stage = new Stage()
  private val spawnPoint = stage.getSpawnPoint
  val player = new Player(spawnPoint(0).toFloat + 0.5f, 0f, spawnPoint(1).toFloat + 0.5f)
  def tick(): Unit = {
    player.tick(this)
  }

  addEventListener(player)
}
