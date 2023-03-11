package logic

import org.joml.Vector3f

/**
 * World stores and updates the game world's state.
 *
 * @param addEventListener
 *   Function for adding event listeners for user input
 */
class World(val addEventListener: EventListener => Unit) {
  val wallHeight = 0.8f
  val stage = new Stage()

  private val spawnPoint = stage.getSpawnPoint
  val lights: Array[Light] = stage.getLightPositions.map(light =>
    new Light(
      Vector3f(light(0).toFloat + 0.5f, wallHeight - 0.1f, light(1).toFloat + 0.5f),
      0.8f,
      light(2),
    ),
  )
  val player = new Player(Vector3f(spawnPoint(0).toFloat + 0.5f, 0f, spawnPoint(1).toFloat + 0.5f))

  def tick(): Unit = {
    player.tick(this)
    lights.foreach(_.tick())
  }

  addEventListener(player)
}
