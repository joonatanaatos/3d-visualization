package logic

import org.joml.Vector3f

class Light(
    initialPosition: Vector3f,
    private var brightness: Float,
    flicker: Boolean = false,
) {
  private val position = Vector3f(initialPosition)
  private val maxBrightness = brightness
  private val minBrightness = 0.1f
  private val flickerTime = 10
  private val flickerProbability = 0.005f
  private var flickerTimer = 0
  private var timeSinceLastFlicker = 0

  def tick(): Unit = {
    if flicker then updateFlicker()
  }

  private def updateFlicker(): Unit = {
    if flickerTimer == 0 then {
      if math.random < flickerProbability * timeSinceLastFlicker then {
        brightness = minBrightness
        flickerTimer = flickerTime
        timeSinceLastFlicker = 0
      } else timeSinceLastFlicker += 1
    } else {
      flickerTimer -= 1
      if flickerTimer == 0 then {
        brightness = maxBrightness
      }
    }
  }

  def getPosition: Vector3f = Vector3f(position)
  def getBrightness: Float = brightness
}
