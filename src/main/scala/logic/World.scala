package logic

import org.joml.Vector3f

import scala.collection.mutable.ArrayBuffer

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
  private val demons: Array[Demon] = stage.getDemonPositions.map(demon =>
    new Demon(
      Vector3f(demon(0).toFloat + 0.5f, 0f, demon(1).toFloat + 0.5f),
    ),
  )
  val player = new Player(Vector3f(spawnPoint(0).toFloat + 0.5f, 0f, spawnPoint(1).toFloat + 0.5f))

  private var gameObjects: ArrayBuffer[GameObject] =
    ArrayBuffer[GameObject](lights ++ demons ++ Array(player): _*)

  def tick(): Unit = {
    gameObjects.foreach(_.tick(this))
    gameObjects = gameObjects.filter(!_.isDead)
  }

  def getGameObjects: Array[GameObject] = gameObjects.toArray

  addEventListener(player)
}
