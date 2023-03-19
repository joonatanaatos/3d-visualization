package graphics

import logic.{CursorListener, EventListener, KeyListener}
import org.joml.Vector2f
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.{
  Callbacks,
  GLFWCursorPosCallbackI,
  GLFWErrorCallback,
  GLFWFramebufferSizeCallbackI,
  GLFWKeyCallbackI,
  GLFWMouseButtonCallbackI,
}
import org.lwjgl.system
import org.lwjgl.system.{MemoryStack, MemoryUtil}

import scala.collection.mutable.ArrayBuffer

/**
 * Window represents a GLFW-window and provides an interface for interacting with it. It handles
 * most GLFW-related tasks.
 *
 * @param title
 *   Window title
 * @param width
 *   Window width
 * @param height
 *   Window height
 */
class Window(val title: String, var width: Int, var height: Int) {
  private var windowHandle: Long = -1
  private val keyListeners: ArrayBuffer[KeyListener] = ArrayBuffer()
  private val cursorListeners: ArrayBuffer[CursorListener] = ArrayBuffer()

  private var resized = false
  private var cursorAttached = false

  initGLFW()
  createWindow()

  private def initGLFW(): Unit = {
    // Setup error callback to System.err
    GLFWErrorCallback.createPrint(System.err).set()

    // Initialize GLFW
    if !glfwInit() then {
      throw new IllegalStateException("Unable to initialize GLFW")
    }

    // Enable window hints
    glfwDefaultWindowHints()
    // Keep the window hidden after creation
    glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE)
    // Prepare buffer for MSAA
    glfwWindowHint(GLFW_SAMPLES, 4)
  }

  private def createWindow(): Unit = {
    // Get the window handle
    windowHandle = glfwCreateWindow(width, height, title, MemoryUtil.NULL, MemoryUtil.NULL)
    if windowHandle == MemoryUtil.NULL then {
      throw new RuntimeException("Failed to create GLFW window")
    }

    // Setup event callbacks
    glfwSetKeyCallback(windowHandle, keyCallback())
    glfwSetCursorPosCallback(windowHandle, cursorPosCallback())
    glfwSetMouseButtonCallback(windowHandle, mouseButtonCallback())
    glfwSetFramebufferSizeCallback(windowHandle, frameBufferSizeCallback())

    // Center the window
    val stack = MemoryStack.stackPush()
    try {
      // Create 2 pointers for window width and height ( int* )
      val windowWidth = stack.mallocInt(1)
      val windowHeight = stack.mallocInt(1)
      // Get window size
      glfwGetWindowSize(windowHandle, windowWidth, windowHeight)
      // Get monitor size
      val videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor())
      // Set window position
      glfwSetWindowPos(
        windowHandle,
        (videoMode.width() - windowWidth.get(0)) / 2,
        (videoMode.height() - windowHeight.get(0)) / 2,
      )
    } finally {
      stack.pop()
    }

    // Make the OpenGL context current
    glfwMakeContextCurrent(windowHandle)
    // Enable v-sync
    glfwSwapInterval(1)
    // Show window
    glfwShowWindow(windowHandle)
  }

  private def keyCallback(): GLFWKeyCallbackI = {
    (_: Long, key: Int, _: Int, action: Int, _: Int) =>
      if key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE then {
        glfwSetWindowShouldClose(windowHandle, true)
      }
      keyListeners.foreach(listener => listener.onKeyPress(key, action))
  }

  private def cursorPosCallback(): GLFWCursorPosCallbackI = {
    (_: Long, xPos: Double, yPos: Double) =>
      if cursorAttached then {
        val middlePoint = (width / 2f, height / 2f)
        val difference = Vector2f(xPos.toFloat - middlePoint(0), yPos.toFloat - middlePoint(1))
        glfwSetCursorPos(windowHandle, middlePoint(0), middlePoint(1))
        cursorListeners.foreach(listener => listener.onCursorMove(difference))
      }
  }

  private def mouseButtonCallback(): GLFWMouseButtonCallbackI = {
    (_: Long, button: Int, action: Int, _: Int) =>
      if action == GLFW_PRESS && button == GLFW_MOUSE_BUTTON_1 then {
        // Attach or detach cursor from screen
        val cursorVisible = glfwGetInputMode(windowHandle, GLFW_CURSOR) == GLFW_CURSOR_NORMAL
        glfwSetInputMode(
          windowHandle,
          GLFW_CURSOR,
          if cursorVisible then GLFW_CURSOR_HIDDEN else GLFW_CURSOR_NORMAL,
        )
        if cursorVisible then {
          glfwSetCursorPos(windowHandle, width / 2, height / 2)
        }
        cursorAttached = cursorVisible
      }
  }

  private def frameBufferSizeCallback(): GLFWFramebufferSizeCallbackI = { (_, width, height) =>
    this.width = width
    this.height = height
    resized = true
  }

  def swapBuffers(): Unit = {
    glfwSwapBuffers(windowHandle)
  }

  def addEventListener(listener: EventListener): Unit = {
    listener match {
      case l: KeyListener => keyListeners += l
      case _              =>
    }
    listener match {
      case l: CursorListener => cursorListeners += l
      case _                 =>
    }
  }

  def shouldClose(): Boolean = {
    glfwWindowShouldClose(windowHandle)
  }

  def pollEvents(): Unit = {
    glfwPollEvents()
  }

  def wasResized(): Boolean = {
    if resized then {
      resized = false
      true
    } else false
  }

  def getAspectRatio: Float = {
    width.toFloat / height.toFloat
  }

  def destroy(): Unit = {
    // Destroy the window
    Callbacks.glfwFreeCallbacks(windowHandle)
    glfwDestroyWindow(windowHandle)
    // Terminate GLFW
    glfwTerminate()
    glfwSetErrorCallback(null).free()
  }
}
