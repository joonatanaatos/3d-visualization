package logic

import org.joml.Vector3f

class Light(
    world: World,
    initialPosition: Vector3f,
    private var brightness: Float,
    flicker: Boolean = false,
) extends GameObject(world, initialPosition) {
  private val maxBrightness = brightness
  private val minBrightness = 0.1f
  private val flickerTime = 10
  private val flickerProbability = 0.005f
  private var flickerTimer = 0
  private var timeSinceLastFlicker = 0

  override def tick(): Unit = {
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

  def getBrightness: Float = brightness
}
