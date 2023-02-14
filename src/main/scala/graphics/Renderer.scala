package graphics

import logic.World
import org.lwjgl.opengl.GL11.glViewport

class Renderer(val world: World, val window: Window) {
  private val renderingHelper = RenderingHelper(window)

  def render() = {
    updateViewport()

    val wallPos = world.stage.getWallPos
    wallPos.sub(world.player.getPosition)

    renderingHelper.clear()
    renderingHelper.drawQuadrilateral(wallPos, world.player.getDirection)
  }

  private def updateViewport() = {
    if (window.wasResized()) {
      glViewport(0, 0, window.width, window.height)
    }
  }
}
