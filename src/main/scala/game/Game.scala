package game

import engine.{EngineInterface, GameInterface}
import graphics.{Renderer, Window}
import logic.World

/**
 * Game handles most high-level game logic.
 */
class Game extends GameInterface {
  private val window = new Window("3D-visualisointi", 600, 600)
  private val world = new World(window.addEventListener)
  private val renderer = new Renderer(world, window)
  private var engine: Option[EngineInterface] = None

  override def init(engine: EngineInterface): Unit = {
    this.engine = Option(engine)
  }

  override def render(): Unit = {
    renderer.render()
    window.swapBuffers()
    window.pollEvents()
    if window.shouldClose() then engine.get.stop()
  }

  override def update(): Unit = {
    world.tick()
  }

  override def close(): Unit = {
    renderer.destroy()
    window.destroy()
  }
}
