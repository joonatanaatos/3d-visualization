package engine

import engine.Engine.UNLIMITED

object Engine {
  val UNLIMITED: Int = -1
}

/**
 * The Engine class manages the main thread that the game is running on. It keeps track of time and
 * calculates when update() and render() should be called
 * @param gameInterface
 *   Interface for invoking init(), update() and render() calls
 */
class Engine(val desiredTps: Int, val desiredFps: Int, val gameInterface: GameInterface)
    extends EngineInterface {

  /**
   * Helper class for running tasks in parallel
   * @param desiredTps
   *   Desired amount of invocations per second
   * @param callback
   *   Callback function that will be invoked
   */
  private class Runner(val desiredTps: Int, val callback: () => Unit) extends Runnable {
    private var currentTps: Int = 0

    private def runContinuously() = {
      while Engine.this.running do {
        callback()
      }
    }

    private def runAtRegularIntervals() = {
      var tickCounter = 0
      // Calculate how much time is between each render/update
      val nsPerTick = if desiredTps == 0 then 0 else 1000000000d / desiredTps

      var before = System.nanoTime()
      var now = 0L
      var unprocessedTicks = 0d
      var unprocessedFrames = 0d

      var tpsTimer = System.currentTimeMillis()

      while Engine.this.running do {
        now = System.nanoTime()
        unprocessedTicks += (now - before) / nsPerTick
        before = now

        if unprocessedTicks >= 1 then {
          callback()
          tickCounter += 1
          unprocessedTicks -= Math.floor(unprocessedTicks)
        }

        if (System.currentTimeMillis() - tpsTimer >= 1000) {
          currentTps = tickCounter
          tpsTimer = System.currentTimeMillis()
          tickCounter = 0
        }

        // Busy waiting
        Thread.sleep(2)
      }
    }

    override def run(): Unit = {
      if desiredTps == UNLIMITED then runContinuously()
      else runAtRegularIntervals()
    }

    def getCurrentTps = currentTps
  }

  private var running = false
  private var printFps = false

  // Create runners
  private val logicRunner = new Runner(desiredTps, gameInterface.update)
  private val rendererRunner = new Runner(desiredFps, gameInterface.render)

  // Start the engine
  override def start(): Unit = {
    running = true
    gameInterface.init(this)
    // Start runners
    val logicThread = new Thread(logicRunner)
    logicThread.start()
    // The renderer is run on the original thread, so that the OpenGL context doesn't change
    rendererRunner.run()
    logicThread.join()
  }

  override def stop(): Unit = {
    running = false
  }

  override def printFps(print: Boolean): Unit = {
    printFps = print
  }

  override def getPrintFps: Boolean = printFps

  override def getFPS: Int = rendererRunner.getCurrentTps

  override def getTPS: Int = logicRunner.getCurrentTps
}
