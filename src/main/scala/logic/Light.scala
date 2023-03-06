package logic

import org.joml.Vector3f

class Light(initialPosition: Vector3f, private var brightness: Float) {
  private val position = Vector3f(initialPosition)

  def getPosition: Vector3f = Vector3f(position)
  def getBrightness: Float = brightness
}
