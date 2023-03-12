package logic

import org.joml.Vector3f

class Demon(initialPosition: Vector3f) extends GameObject(initialPosition) {
  val height = 0.6f
  val size = 0.75f
  private val movementSpeed = 0.4f
  private var direction = 0f
  private val attackThreshold = 8f
  private val scareThreshold = 0.5f

  private def move(world: World): Unit = {
    val change = Vector3f(0f, 0f, movementSpeed).rotateY(direction)
    if world.stage.canBeInPosition((position.x + change.x, position.z), size) then
      position.add(Vector3f(change.x, 0f, 0f))
    if world.stage.canBeInPosition((position.x, position.z + change.z), size) then
      position.add(Vector3f(0f, 0f, change.z))
  }

  override def tick(world: World): Unit = {
    val playerPos = world.player.getPosition
    val difference = playerPos.sub(position)
    direction = -difference.angleSigned(Vector3f(0f, 0f, 1f), Vector3f(0f, 1f, 0f))
    val distance = difference.length
    if distance < attackThreshold then move(world)
    if distance < scareThreshold then isDead = true
  }

  def getDirection: Float = direction
}
