package graphics

import logic.World

class Renderer(val world: World, val window: Window) {
  private val renderingHelper = RenderingHelper(window)

  def render() = {
    val wallPos = world.stage.wallPos
    val xPos = wallPos(0) - world.player.xPos
    val yPos = wallPos(1) - world.player.yPos
    val zPos = -(wallPos(2) - world.player.zPos)

    renderingHelper.clear()
    renderingHelper.drawQuadrilateral(xPos, yPos, zPos)
  }
}
