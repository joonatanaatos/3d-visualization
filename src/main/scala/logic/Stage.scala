package logic

import org.joml.Vector3f

class Stage {
  private val wallPos = Vector3f(0f, 0f, -3f)
  def getWallPos = Vector3f(wallPos)
}
