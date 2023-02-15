package graphics

import logic.World
import org.lwjgl.opengl.GL11.glViewport
import graphics.Utils.glCheck
import org.joml.{Matrix3f, Vector3f}

/**
 * Renderer draws the World onto the screen
 * @param world
 *   World to be visualized
 * @param window
 *   Window that the World should be rendered onto
 */
class Renderer(val world: World, val window: Window) {
  private val renderingHelper = RenderingHelper(window)
  private var cameraPosition = Vector3f(0f, 0f, 0f)
  private var cameraDirection = (0f, 0f)

  private val transitionMatrix = Matrix3f().identity().scale(2)

  def render(): Unit = {
    updateViewport()
    updateCameraPosition()
    renderingHelper.clear()

    // Draw
    val (horizontalWalls, verticalWalls) = world.stage.getWallPositions
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
    val wallPos = Vector3f(xPos.toFloat, 0f, zPos.toFloat)
    wallPos.mul(transitionMatrix)
    wallPos.sub(cameraPosition)
    val wallAlignment =
      if direction == "horizontal" then Vector3f(1f, 0f, 0f) else Vector3f(0f, 0f, 1f)
    wallPos.add(wallAlignment)

    val angle = if direction == "vertical" then math.Pi.toFloat / 2f else 0f
    val color = Array(0.8f, 0f, 0.7f, 1f)

    renderingHelper.drawQuadrilateral(wallPos, cameraDirection, angle, color)
  }

  private def updateViewport(): Unit = {
    if (window.wasResized()) {
      glCheck { glViewport(0, 0, window.width, window.height) }
    }
  }

  private def updateCameraPosition(): Unit = {
    cameraPosition = world.player.getPosition.mul(transitionMatrix)
    cameraDirection = world.player.getDirection
  }

  def destroy(): Unit = {
    renderingHelper.destroy()
  }
}
