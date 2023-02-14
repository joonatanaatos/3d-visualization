package engine

/**
 * The GameInterface is for the Engine to invoke init(), update(), and render() calls
 */
trait GameInterface {

  /** Called once upon starting the engine */
  def init(engine: EngineInterface): Unit

  /** Called repetedly with an interval defined by tps */
  def update(): Unit

  /** Called repetedly with an interval defined by fps */
  def render(): Unit
}
