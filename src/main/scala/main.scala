import graphics.Window
import graphics.RenderingHelper

@main
def main(): Unit = {
  println("Hello world!")
  val window = new Window("3D-visualisointi", 600, 600)
  val renderingHelper = RenderingHelper(window)
  while (!window.shouldClose()) do {
    renderingHelper.clear()
    renderingHelper.drawQuadrilateral()
    window.swapBuffers()
    window.pollEvents()
  }
}
