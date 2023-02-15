package logic

class Stage {

  private val horizontalWalls: Array[Array[Int]] = Array(
    Array(1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
    Array(0, 1, 0, 1, 0, 0, 1, 0, 1, 0),
    Array(0, 1, 0, 1, 0, 0, 1, 0, 1, 0),
    Array(0, 1, 0, 0, 1, 1, 0, 0, 1, 0),
    Array(1, 1, 0, 1, 0, 0, 1, 0, 1, 1),
    Array(1, 1, 0, 0, 1, 1, 0, 0, 1, 1),
    Array(1, 1, 0, 0, 1, 1, 0, 0, 1, 1),
    Array(1, 1, 0, 0, 1, 1, 0, 0, 1, 1),
    Array(0, 1, 0, 1, 0, 0, 1, 0, 1, 0),
    Array(1, 0, 0, 0, 1, 1, 0, 0, 0, 1),
    Array(0, 1, 1, 1, 0, 0, 1, 1, 1, 0),
    Array(1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
  )

  private val verticalWalls: Array[Array[Int]] = Array(
    Array(1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1),
    Array(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
    Array(1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1),
    Array(1, 0, 0, 1, 0, 1, 0, 1, 0, 0, 1),
    Array(0, 0, 1, 1, 0, 0, 0, 1, 1, 0, 0),
    Array(0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0),
    Array(0, 0, 1, 1, 0, 0, 0, 1, 1, 0, 0),
    Array(1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1),
    Array(1, 0, 1, 0, 0, 0, 0, 0, 1, 0, 1),
    Array(1, 0, 0, 1, 0, 1, 0, 1, 0, 0, 1),
    Array(1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1),
  )

  def getWallPositions: (Array[Array[Int]], Array[Array[Int]]) = (horizontalWalls, verticalWalls)
}
