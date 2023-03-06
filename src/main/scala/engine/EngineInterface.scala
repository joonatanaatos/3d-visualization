package engine

/**
 * The provided interface for accessing the Engine
 */
trait EngineInterface {
  def start(): Unit
  def stop(): Unit
  def setDebugPrints(print: Boolean): Unit
  def getDebugPrints: Boolean
  def getFPS: Int
  def getTPS: Int
}
