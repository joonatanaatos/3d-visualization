package logic

import audio.{AudioPlayer, Sound}
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
  private var started = false

  private val scareTime = 60
  private var scareTimer = 0
  private var scareCause: Option[Demon] = None

  private val spawnPoint = stage.getSpawnPoint

  val lights: Array[Light] = stage.getLightPositions.map(light =>
    new Light(
      this,
      Vector3f(light(0).toFloat + 0.5f, wallHeight - 0.1f, light(1).toFloat + 0.5f),
      1.6f,
      light(2),
    ),
  )
  private val demons: Array[Demon] = stage.getDemonPositions.map(demon =>
    new Demon(
      this,
      Vector3f(demon(0).toFloat + 0.5f, 0f, demon(1).toFloat + 0.5f),
    ),
  )
  private val cubes: Array[Cube] = stage.getCubePositions.map(cube =>
    new Cube(
      this,
      Vector3f(cube(0).toFloat + 0.5f, wallHeight / 2, cube(1).toFloat + 0.5f),
    ),
  )
  val player =
    new Player(this, Vector3f(spawnPoint(0).toFloat + 0.5f, 0f, spawnPoint(1).toFloat + 0.5f))

  private var gameObjects: ArrayBuffer[GameObject] =
    ArrayBuffer[GameObject](lights ++ demons ++ cubes ++ Array(player): _*)

  addEventListener(player)

  def tick(): Unit = {
    if !started then {
      started = true
    }
    updateTimers()
    gameObjects.foreach(_.tick())
    gameObjects = gameObjects.filter(!_.isDead)
  }

  private def createNewDemon(): Demon = {
    val playerPos = player.getPosition
    val spawnPositions = stage.getAllAvailablePositions.filter(pos => {
      val distance = Vector3f(pos(0).toFloat + 0.5f, 0f, pos(1).toFloat + 0.5f).distance(playerPos)
      distance > Demon.attackThreshold + 5
    })
    val spawnPosition = spawnPositions((Math.random() * spawnPositions.length).toInt)
    new Demon(this, Vector3f(spawnPosition(0).toFloat + 0.5f, 0f, spawnPosition(1).toFloat + 0.5f))
  }

  private def updateTimers(): Unit = {
    if (scareTimer > 0) {
      scareTimer -= 1
      if scareTimer == 0 then {
        scareCause = None
        gameObjects += createNewDemon()
        AudioPlayer.stop(Sound.Demon)
      }
    }
  }

  def startScare(demon: Demon): Unit = {
    scareTimer = scareTime
    scareCause = Option(demon)
    AudioPlayer.setVolume(Sound.Demon, 0f)
    AudioPlayer.loop(Sound.Demon)
  }

  def hasStarted: Boolean = started

  def getGameObjects: Array[GameObject] = gameObjects.toArray

  def getScareStatus: (Int, Option[Demon]) = (scareTimer, scareCause)
}
