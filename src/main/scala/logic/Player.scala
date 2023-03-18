package logic

import audio.AudioPlayer
import org.joml.{Vector2f, Vector3f}
import org.lwjgl.glfw.GLFW.{
  GLFW_KEY_A,
  GLFW_KEY_D,
  GLFW_KEY_DOWN,
  GLFW_KEY_LEFT,
  GLFW_KEY_LEFT_SHIFT,
  GLFW_KEY_RIGHT,
  GLFW_KEY_S,
  GLFW_KEY_UP,
  GLFW_KEY_W,
  GLFW_PRESS,
  GLFW_RELEASE,
}

import scala.collection.mutable.Set

/**
 * Player represents the player in the game world. It stores and updates the player position based
 * on user input.
 * @param initialPosition
 *   Initial player position
 */
class Player(initialPosition: Vector3f)
    extends GameObject(initialPosition),
      KeyListener,
      CursorListener {
  private var direction = (-math.Pi.toFloat, 0f)
  private var normalizedVelocity = Vector3f()
  private var velocity = Vector3f()

  private val pressedKeys: Set[Int] = Set()
  private val runningSpeed = 0.03f
  private val walkingSpeed = 0.02f
  private var movementSpeed = walkingSpeed
  private val rotationSpeed = 0.0008f
  private val size = 0.2f

  private var stepTimer = 0
  private val stepFactor = 0.7f
  private val stepThreshold = 0.001f

  override def tick(world: World): Unit = {
    checkInput()
    move(world)
    updateSounds()
  }

  private def move(world: World): Unit = {
    normalizedVelocity = getNormalizedVelocity
    val previousPosition = Vector3f(position)
    val change = normalizedVelocity.mul(movementSpeed)
    if world.stage.canBeInPosition((position.x + change.x, position.z), size) then
      position.add(Vector3f(change.x, 0f, 0f))
    if world.stage.canBeInPosition((position.x, position.z + change.z), size) then
      position.add(Vector3f(0f, 0f, change.z))
    velocity = Vector3f(position).sub(previousPosition)
  }

  private def getNormalizedVelocity: Vector3f = {
    val velocity = Vector3f()
    if pressedKeys.contains(GLFW_KEY_UP) || pressedKeys.contains(GLFW_KEY_W) then {
      velocity.z -= 1
    }
    if pressedKeys.contains(GLFW_KEY_DOWN) || pressedKeys.contains(GLFW_KEY_S) then {
      velocity.z += 1
    }
    if pressedKeys.contains(GLFW_KEY_RIGHT) || pressedKeys.contains(GLFW_KEY_D) then {
      velocity.x += 1
    }
    if pressedKeys.contains(GLFW_KEY_LEFT) || pressedKeys.contains(GLFW_KEY_A) then {
      velocity.x -= 1
    }
    velocity.rotateY(-direction(0))
    velocity.normalize()
    if velocity.isFinite then velocity else Vector3f()
  }

  private def checkInput(): Unit = {
    if pressedKeys.contains(GLFW_KEY_LEFT_SHIFT) then {
      movementSpeed = runningSpeed
    } else {
      movementSpeed = walkingSpeed
    }
  }

  private def updateSounds(): Unit = {
    val currentVelocity = velocity.length()
    if currentVelocity > stepThreshold then {
      val stepTime = (stepFactor / currentVelocity).toInt
      if stepTimer == 0 then {
        stepTimer = stepTime
        AudioPlayer.playRandom(AudioPlayer.stepSounds)
      } else {
        stepTimer = math.min(stepTimer - 1, stepTime)
      }
    } else if stepTimer != 0 then {
      AudioPlayer.stop(AudioPlayer.stepSounds)
      stepTimer = 0
    }
  }

  def getDirection: (Float, Float) = direction

  override def onKeyPress(key: Int, action: Int): Unit = {
    action match {
      case GLFW_PRESS =>
        pressedKeys += key
      case GLFW_RELEASE =>
        pressedKeys -= key
      case _ =>
    }
  }

  override def onCursorMove(difference: (Float, Float)): Unit = {
    val xDir = direction(0) + difference(0) * rotationSpeed
    val yDir = direction(1) + difference(1) * rotationSpeed
    direction = (
      xDir % (2f * math.Pi.toFloat),
      math.max(math.min(yDir, math.Pi.toFloat / 2f), -math.Pi.toFloat / 2f),
    )
  }
}
