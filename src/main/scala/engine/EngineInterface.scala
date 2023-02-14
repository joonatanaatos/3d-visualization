package engine

/**
 * The provided interface for accessing the Engine
 */
trait EngineInterface {
  def start(): Unit
  def stop(): Unit
  def printFps(print: Boolean): Unit
  def getPrintFps: Boolean
  def getFPS: Int
  def getTPS: Int
}
