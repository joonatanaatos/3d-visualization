package logic

import org.joml.Vector3f

class Cube(world: World, initialPosition: Vector3f) extends GameObject(world, initialPosition) {
  private val rotation = Vector3f(0, 0, 0)
  private val rotationSpeed = Vector3f(0.01f, 0.015f, 0.005f)
  private var floatState = 0f
  private val floatSpeed = 0.03f
  val size = 0.3f
  private val maxDifference = size / 3f

  override def tick(): Unit = {
    rotation.add(rotationSpeed)
    rotation.x = rotation.x % (math.Pi.toFloat * 2)
    rotation.y = rotation.y % (math.Pi.toFloat * 2)
    rotation.z = rotation.z % (math.Pi.toFloat * 2)
    floatState = (floatState + floatSpeed) % (math.Pi.toFloat * 2f)
    val difference = math.sin(floatState).toFloat * maxDifference
    position.y = initialPosition.y + difference
  }

  def getRotation: Vector3f = Vector3f(rotation)
}
