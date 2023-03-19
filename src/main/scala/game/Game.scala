package game

import audio.{AudioPlayer, Sound}
import engine.{EngineInterface, GameInterface}
import graphics.{Renderer, Window}
import logic.World

/**
 * Game handles most high-level game logic.
 */
class Game extends GameInterface {
  private val window = new Window("3D-visualisointi", 1000, 600)
  val world = new World(window.addEventListener)
  val menu = new Menu(this)
  window.addEventListener(menu)

  private val renderer = new Renderer(this, window)
  private var engine: Option[EngineInterface] = None
  private var state: GameState = GameState.Menu

  private val transitionTime = 300
  private var transitionTimer = 0

  override def init(engine: EngineInterface): Unit = {
    this.engine = Option(engine)
    AudioPlayer.loop(Sound.BackgroundMusic)
  }

  override def render(): Unit = {
    renderer.render(state)
    window.swapBuffers()
    window.pollEvents()
    if window.shouldClose() then engine.get.stop()
  }

  override def update(): Unit = {
    state match {
      case GameState.Menu =>
        menu.update()
      case GameState.Game =>
        world.tick()
      case _ =>
    }
    updateTimers()
  }

  override def close(): Unit = {
    renderer.destroy()
    window.destroy()
  }

  def updateTimers(): Unit = {
    if transitionTimer > 0 then {
      transitionTimer -= 1
      if transitionTimer == 0 then {
        state match {
          case GameState.MenuToGame =>
            state = GameState.Game
            menu.reset()
          case _ =>
        }
      }
    }
  }

  def startGame(): Unit = {
    if state == GameState.Menu then {
      state = GameState.MenuToGame
      transitionTimer = transitionTime
    }
  }

  def getTransitionProgress: Float = {
    (transitionTime - transitionTimer).toFloat / transitionTime.toFloat
  }
}
