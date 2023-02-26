package logic

/**
 * Direction represents one of the four points of the compass
 */
enum Direction(val angle: Float, val horizontal: Boolean) {
  case East extends Direction(0f, true)
  case North extends Direction(math.Pi.toFloat / 2f, false)
  case West extends Direction(math.Pi.toFloat, true)
  case South extends Direction(math.Pi.toFloat * 3f / 2f, false)

  val vertical: Boolean = !horizontal
}
