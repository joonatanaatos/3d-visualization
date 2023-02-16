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

  private def squareOverlapsWall(
      wallPos: (Int, Int),
      squarePos: (Float, Float),
      size: Float,
      direction: String,
  ): Boolean = {
    val dx = squarePos(0) - wallPos(0)
    val dz = squarePos(1) - wallPos(1)
    if direction == "horizontal" then {
      dx > -size && dx < 1 + size && dz > -size && dz < size
    } else {
      dx > -size && dx < size && dz > -size && dz < 1 + size
    }
  }

  def canBeInPosition(pos: (Float, Float), size: Float): Boolean = {
    val xPos = math.floor(pos(0)).toInt
    val zPos = math.floor(pos(1)).toInt
    val wallsToCheck =
      Array(
        (0, 0, "horizontal"),
        (0, 0, "vertical"),
        (1, 0, "horizontal"),
        (1, 0, "vertical"),
        (0, 1, "horizontal"),
        (0, 1, "vertical"),
        (1, 1, "horizontal"),
        (1, 1, "vertical"),
        (-1, 0, "horizontal"),
        (-1, 1, "horizontal"),
        (0, -1, "vertical"),
        (1, -1, "vertical"),
      );
    !wallsToCheck.exists(wall => {
      val wallX = xPos + wall(0)
      val wallZ = zPos + wall(1)
      val walls = if wall(2) == "horizontal" then horizontalWalls else verticalWalls
      if wallX >= 0 && wallZ >= 0 && wallX < walls.head.length && wallZ < walls.length
      then {
        val wallExists: Boolean = walls(wallZ)(wallX) == 1
        wallExists && squareOverlapsWall((wallX, wallZ), pos, size, wall(2))
      } else false
    })
  }
}
