package graphics

import logic.World
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
  private val cameraRelativeToPlayer = Vector3f(0f, 0.8f, 0f)
  private var cameraPosition = Vector3f(cameraRelativeToPlayer)
  private var cameraDirection = (0f, 0f)

  private val transitionMatrix = Matrix3f().scale(2)
  private val wallShapeMatrix = Matrix4f().scale(1f, 0.8f, 1f).translate(0f, 1f, 0f)
  private def floorShapeMatrix(width: Float, depth: Float) =
    Matrix4f().scale(width, 1f, depth).translate(1f, 0f, 1f).rotateX(math.Pi.toFloat / 2f)

  def render(): Unit = {
    updateViewport()
    updateCameraPosition()
    renderingHelper.clear()

    // Begin draw
    val (horizontalWalls, verticalWalls) = world.stage.getWallPositions
    drawFloor(horizontalWalls.head.length, horizontalWalls.length - 1)
    drawWallArray(horizontalWalls, "horizontal")
    drawWallArray(verticalWalls, "vertical")
  }

  private def drawWallArray(
      walls: Array[Array[Int]],
      direction: "horizontal" | "vertical",
  ): Unit = {
    for (rowIndex <- walls.indices) {
      val wallRow = walls(rowIndex)
      for (columnIndex <- wallRow.indices) {
        val hasWall = wallRow(columnIndex) == 1
        if hasWall then drawWall(columnIndex, rowIndex, direction)
      }
    }
  }

  private def drawWall(xPos: Int, zPos: Int, direction: "horizontal" | "vertical"): Unit = {
    // Calculate wall pos
    val wallPos = Vector3f(xPos.toFloat, 0f, zPos.toFloat)
    wallPos.mul(transitionMatrix)
    wallPos.sub(cameraPosition)
    val wallAlignment =
      if direction == "horizontal" then Vector3f(1f, 0f, 0f) else Vector3f(0f, 0f, 1f)
    wallPos.add(wallAlignment)
    // Calculate wall angle
    val angle = if direction == "vertical" then math.Pi.toFloat / 2f else 0f

    // 1. Scale, 2. Rotate, 3. Translate
    val modelMatrix = Matrix4f()
    modelMatrix.translate(wallPos)
    modelMatrix.rotateY(angle)
    modelMatrix.mul(wallShapeMatrix)

    val color = Array(0.8f, 0f, 0.7f, 1f)

    renderingHelper.drawQuadrilateral(modelMatrix, cameraDirection, color)
  }

  private def drawFloor(width: Int, depth: Int): Unit = {
    val floorPos = Vector3f()
    floorPos.sub(cameraPosition)

    val modelMatrix = Matrix4f()
    modelMatrix.translate(floorPos)
    modelMatrix.mul(floorShapeMatrix(width.toFloat, depth.toFloat))

    val color = Array(0.8f, 0.7f, 1f, 1f)

    renderingHelper.drawQuadrilateral(modelMatrix, cameraDirection, color)
  }

  private def updateViewport(): Unit = {
    if (window.wasResized()) {
      glCheck { glViewport(0, 0, window.width, window.height) }
    }
  }

  private def updateCameraPosition(): Unit = {
    cameraPosition = world.player.getPosition.mul(transitionMatrix).add(cameraRelativeToPlayer)
    cameraDirection = world.player.getDirection
  }

  def destroy(): Unit = {
    renderingHelper.destroy()
  }
}
