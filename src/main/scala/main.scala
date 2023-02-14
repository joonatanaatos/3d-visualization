import graphics.{Renderer, RenderingHelper, Window}
import logic.World

@main
def main(): Unit = {
  val window = new Window("3D-visualisointi", 600, 600)
  val world = new World(window.addEventListener)
  val renderer = new Renderer(world, window)

  while (!window.shouldClose()) do {
    world.tick()
    renderer.render()
    window.swapBuffers()
    window.pollEvents()
  }
}
