import logic.{CursorListener, EventListener, KeyListener, World}
import org.joml.{Vector2f, Vector3f}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.concurrent.Eventually
import org.lwjgl.glfw.GLFW

import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets

class LogicSpec extends AnyFlatSpec with Matchers {
  behavior of "World"

  var listenerOption: Option[EventListener] = None
  val world = new World(l => listenerOption = Option(l))

  "World" should "add an event listener" in {
    Eventually.eventually {
      listenerOption.isDefined should be(true)
    }
  }

  val listener: Object =
    listenerOption.getOrElse(() => throw new RuntimeException("listener was not defined"))
  val keyListener: KeyListener = listener.asInstanceOf[KeyListener]
  val cursorListener: CursorListener = listener.asInstanceOf[CursorListener]

  "Player" should "move on key press" in {
    val originalPosition = world.player.getPosition
    keyListener.onKeyPress(GLFW.GLFW_KEY_W, GLFW.GLFW_PRESS)
    world.tick()
    keyListener.onKeyPress(GLFW.GLFW_KEY_W, GLFW.GLFW_RELEASE)
    val newPosition = world.player.getPosition
    newPosition.sub(originalPosition).length() should not be 0
  }

  "Player" should "eventually be stopped by a wall" in {
    keyListener.onKeyPress(GLFW.GLFW_KEY_W, GLFW.GLFW_PRESS)
    var i = 0
    var previousPosition = world.player.getPosition
    var currentPosition = Vector3f()
    def positionChange = Vector3f(currentPosition).sub(previousPosition).length()
    while i < 10000 && positionChange != 0 do {
      world.tick()
      previousPosition = currentPosition
      currentPosition = world.player.getPosition
      i += 1
    }
    keyListener.onKeyPress(GLFW.GLFW_KEY_W, GLFW.GLFW_RELEASE)
    positionChange should be(0)
  }

  "Player" should "rotate on cursor move only when the world is ticked" in {
    val originalDirection = world.player.getDirection
    cursorListener.onCursorMove(Vector2f(5, 5))
    val directionBeforeTick = world.player.getDirection
    directionBeforeTick should be(originalDirection)
    world.tick()
    val directionAfterTick = world.player.getDirection
    directionAfterTick should not be originalDirection
  }
}
