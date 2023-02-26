package logic

import logic.Direction
import org.yaml.snakeyaml.Yaml
import java.io.{BufferedReader, InputStreamReader}
import java.util.stream.Collectors
import java.util.Map

class Wall(val direction: Direction)

/**
 * Stage represents the physical world in the game world.
 */
class Stage {
  // Load and parse world file
  private val worldFileSource = getClass.getResourceAsStream("/world.yml")
  private val worldFile = new Yaml().load[Map[String, Any]](worldFileSource)
  // Walls
  private val stageString = worldFile.get("stage").asInstanceOf[String]
  private val (horizontalWalls, verticalWalls) = generateWalls(stageString)
  // Spawn point
  private val spawnPointMap = worldFile.get("spawn").asInstanceOf[Map[String, Any]]
  private val spawnPoint = (
    spawnPointMap.get("x").asInstanceOf[Int],
    spawnPointMap.get("y").asInstanceOf[Int],
  )

  def getWallPositions: (Array[Array[Option[Wall]]], Array[Array[Option[Wall]]]) =
    (horizontalWalls, verticalWalls)

  def getSpawnPoint: (Int, Int) = spawnPoint

  private def generateWalls(
      stageString: String,
  ): (Array[Array[Option[Wall]]], Array[Array[Option[Wall]]]) = {
    // Parse stage string
    val stageGrid = stageString.split("\n").map(_.split("\\s+"))
    val height = stageGrid.length
    val width = stageGrid.head.length

    // There are always grid size + 1 walls
    val horizontalWalls = Array.fill[Option[Wall]](height + 1, width + 1)(None)
    val verticalWalls = Array.fill[Option[Wall]](height + 1, width + 1)(None)

    // Returns true if there is a wall at the given position
    def gridHasWallAt(x: Int, y: Int): Boolean = {
      if y < 0 || y >= height || x < 0 || x >= width then false
      else stageGrid(y)(x) == "1"
    }

    // Go through all positions
    for (y <- 0 until height) {
      for (x <- 0 until width) {
        if gridHasWallAt(x, y) then {
          // Only add a wall if the neighbouring position doesn't have a wall
          Array(1, -1).foreach(d => {
            if !gridHasWallAt(x + d, y) then {
              verticalWalls(y)(x + (d + 1) / 2) =
                Option(Wall(if d == 1 then Direction.East else Direction.West))
            }
            if !gridHasWallAt(x, y + d) then {
              horizontalWalls(y + (d + 1) / 2)(x) =
                Option(Wall(if d == 1 then Direction.South else Direction.North))
            }
          })
        }
      }
    }
    (horizontalWalls, verticalWalls)
  }

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
        val wallExists: Boolean = walls(wallZ)(wallX).isDefined
        wallExists && squareOverlapsWall((wallX, wallZ), pos, size, wall(2))
      } else false
    })
  }
}
