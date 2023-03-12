package logic

import org.joml.Vector3f

abstract class GameObject(initialPosition: Vector3f) {
  protected val position: Vector3f = Vector3f(initialPosition)
  var isDead: Boolean = false
  def tick(world: World): Unit
  def getPosition: Vector3f = Vector3f(position)
}
