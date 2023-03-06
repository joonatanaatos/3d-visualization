package graphics

import logic.{World, Stage, Wall}
import org.lwjgl.opengl.GL11.glViewport
import graphics.Utils.glCheck
import org.joml.{Matrix4f, Matrix3f, Vector3f}

/**
 * Renderer draws the World onto the screen
 * @param world
 *   World to be visualized
 * @param window
 *   Window that the World should be rendered onto
 */
class Renderer(val world: World, val window: Window) {
  private val renderingHelper = RenderingHelper(window)
  private val cameraRelativeToPlayer = Vector3f(0f, 0.4f, 0f)
  private var cameraPosition = Vector3f(cameraRelativeToPlayer)
  private var cameraDirection = (0f, 0f)

  private val wallHeight = world.wallHeight
  private val wallShapeMatrix =
    Matrix4f().scale(1f, wallHeight, 1f).scale(0.5f).translate(0f, 1f, 0f)
  private val floorShapeMatrix =
    Matrix4f().scale(0.5f).translate(1f, 0f, 1f).rotateX(math.Pi.toFloat / 2f)

  private val wallTexture = new Texture("wall")
  private val floorTexture = new Texture("floor")

  def render(): Unit = {
    updateViewport()
    updateCameraPosition()
    updateLighting()
    renderingHelper.clear()

    // Begin draw
    val (horizontalWalls, verticalWalls) = world.stage.getWallPositions
    drawFloorAndCeiling(horizontalWalls.head.length - 1, horizontalWalls.length - 1)
    drawWallArray(horizontalWalls)
    drawWallArray(verticalWalls)
  }

  private def drawWallArray(
      walls: Array[Array[Option[Wall]]],
  ): Unit = {
    for (rowIndex <- walls.indices) {
      val wallRow = walls(rowIndex)
      for (columnIndex <- wallRow.indices) {
        val wall = wallRow(columnIndex)
        if wall.isDefined then drawWall(columnIndex, rowIndex, wall.get)
      }
    }
  }

  private def drawWall(xPos: Int, zPos: Int, wall: Wall): Unit = {
    // Calculate wall pos
    val wallPos = Vector3f(xPos.toFloat, 0f, zPos.toFloat)
    wallPos.sub(cameraPosition)
    val wallAlignment =
      if wall.direction.horizontal then Vector3f(0f, 0f, 0.5f) else Vector3f(0.5f, 0f, 0f)
    wallPos.add(wallAlignment)
    // Calculate wall angle
    val angle = wall.direction.angle + math.Pi.toFloat / 2f
    val normal = Vector3f(1f, 0f, 0f).rotateY(wall.direction.angle).normalize()

    // 1. Scale, 2. Rotate, 3. Translate
    val modelMatrix = Matrix4f()
    modelMatrix.translate(wallPos)
    modelMatrix.rotateY(angle)
    modelMatrix.mul(wallShapeMatrix)

    renderingHelper.drawImage(
      modelMatrix,
      cameraDirection,
      wallTexture,
      normal,
    )
  }

  private def drawFloorAndCeiling(width: Int, depth: Int): Unit = {
    val position = Vector3f()
    position.sub(cameraPosition)

    for (x <- 0 until width) {
      for (z <- 0 until depth) {
        for (y <- Array(0f, wallHeight)) {
          val modelMatrix = Matrix4f()
          modelMatrix.translate(position)
          modelMatrix.translate(x, y, z)
          modelMatrix.mul(floorShapeMatrix)

          renderingHelper.drawImage(
            modelMatrix,
            cameraDirection,
            floorTexture,
            Vector3f(0f, if y == 0f then 1f else -1f, 0f),
          )
        }
      }
    }
  }

  private def updateViewport(): Unit = {
    if (window.wasResized()) {
      glCheck { glViewport(0, 0, window.width, window.height) }
    }
  }

  private def updateLighting(): Unit = {
    renderingHelper.setAmbientLightBrightness(0.01f)
    renderingHelper.setPointLights(
      world.lights
        .map(light => (light.getPosition.sub(cameraPosition), light.getBrightness))
        .sortBy(light => light(0).length())
        .take(renderingHelper.maxNumberOfLights),
    )
  }

  private def updateCameraPosition(): Unit = {
    cameraPosition = world.player.getPosition.add(cameraRelativeToPlayer)
    cameraDirection = world.player.getDirection
  }

  def destroy(): Unit = {
    renderingHelper.destroy()
  }
}
