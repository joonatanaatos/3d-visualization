package logic

import org.joml.{Vector2f, Vector3f}
import org.lwjgl.glfw.GLFW.{
  GLFW_KEY_A,
  GLFW_KEY_D,
  GLFW_KEY_DOWN,
  GLFW_KEY_LEFT,
  GLFW_KEY_RIGHT,
  GLFW_KEY_S,
  GLFW_KEY_UP,
  GLFW_KEY_W,
  GLFW_PRESS,
  GLFW_RELEASE,
}

import scala.collection.mutable.Set

class Player(xPos: Float, yPos: Float, zPos: Float) extends KeyListener, CursorListener {
  private val position = Vector3f(xPos, yPos, zPos)
  private var direction = (-math.Pi.toFloat, 0f)

  private val pressedKeys: Set[Int] = Set()
  private val movementSpeed = 0.03f
  private val rotationSpeed = 0.0008f
  private val size = 0.2f

  def tick(world: World): Unit = {
    val velocity = normalizedVelocity()
    val change = velocity.mul(movementSpeed)
    if world.stage.canBeInPosition((position.x + change.x, position.z), size) then
      position.add(Vector3f(change.x, 0f, 0f))
    if world.stage.canBeInPosition((position.x, position.z + change.z), size) then
      position.add(Vector3f(0f, 0f, change.z))
  }

  private def normalizedVelocity(): Vector3f = {
    val velocity = Vector3f().zero()
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
    if velocity.isFinite then velocity else Vector3f().zero()
  }

  def getPosition: Vector3f = Vector3f(position)
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
