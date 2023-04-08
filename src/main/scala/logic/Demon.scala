package logic

import audio.{AudioPlayer, Sound}
import org.joml.Vector3f

object Demon {
  val attackThreshold = 9f
}

class Demon(world: World, initialPosition: Vector3f) extends GameObject(world, initialPosition) {
  val height = 0.6f
  val size = 0.3f
  private val movementSpeed = 0.05f
  private var direction = 0f
  private val attackThreshold = Demon.attackThreshold
  private val scareThreshold = 0.5f
  private var distanceFromPlayer = Float.MaxValue

  override def tick(): Unit = {
    val playerPos = world.player.getPosition
    val difference = playerPos.sub(position)
    direction = -difference.angleSigned(Vector3f(0f, 0f, 1f), Vector3f(0f, 1f, 0f))
    distanceFromPlayer = difference.length
    if distanceFromPlayer < attackThreshold then {
      move()
    }
    if distanceFromPlayer < scareThreshold then {
      isDead = true
      world.startScare(this)
    }
    if distanceFromPlayer < 20 then {
      if !AudioPlayer.isPlaying(Sound.Demon) then {
        AudioPlayer.setVolume(Sound.Demon, -80f)
        AudioPlayer.loop(Sound.Demon)
      }
      updateSound()
    }
  }

  private def move(): Unit = {
    val change = Vector3f(0f, 0f, movementSpeed).rotateY(direction)
    if world.stage.canBeInPosition((position.x + change.x, position.z), size) then
      position.add(Vector3f(change.x, 0f, 0f))
    if world.stage.canBeInPosition((position.x, position.z + change.z), size) then
      position.add(Vector3f(0f, 0f, change.z))
  }

  private def updateSound(): Unit = {
    val volume =
      math.max(-4f * (distanceFromPlayer - scareThreshold), -80f)
    AudioPlayer.setVolume(Sound.Demon, volume)
  }

  def getDirection: Float = direction
}
