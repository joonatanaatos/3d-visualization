package logic

import org.joml.Vector3f

abstract class GameObject(world: World, initialPosition: Vector3f) {
  protected val position: Vector3f = Vector3f(initialPosition)
  var isDead: Boolean = false
  def tick(): Unit
  def getPosition: Vector3f = Vector3f(position)
}
