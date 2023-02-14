package graphics

import org.lwjgl.glfw.GLFW.{
  GLFW_FALSE,
  GLFW_KEY_ESCAPE,
  GLFW_RELEASE,
  GLFW_RESIZABLE,
  GLFW_VISIBLE,
  glfwCreateWindow,
  glfwDefaultWindowHints,
  glfwDestroyWindow,
  glfwGetPrimaryMonitor,
  glfwGetVideoMode,
  glfwGetWindowSize,
  glfwInit,
  glfwMakeContextCurrent,
  glfwPollEvents,
  glfwSetErrorCallback,
  glfwSetKeyCallback,
  glfwSetWindowPos,
  glfwSetWindowShouldClose,
  glfwShowWindow,
  glfwSwapBuffers,
  glfwSwapInterval,
  glfwTerminate,
  glfwWindowHint,
  glfwWindowShouldClose,
}
import org.lwjgl.glfw.{Callbacks, GLFWErrorCallback, GLFWKeyCallbackI}
import org.lwjgl.system
import org.lwjgl.system.{MemoryStack, MemoryUtil}

import scala.collection.mutable.ArrayBuffer

/**
 * The window class handles most GLFW-related tasks
 *
 * @param title
 *   Window title
 * @param width
 *   Window width
 * @param height
 *   Window height
 */
class Window(val title: String, val width: Int, val height: Int) extends GLFWKeyCallbackI {
  private var windowHandle: Long = -1
  private val eventListeners: ArrayBuffer[GLFWKeyCallbackI] = ArrayBuffer()

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
    // Prevet user from resizing window
    glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE)
  }

  private def createWindow(): Unit = {
    // Get the window handle
    windowHandle = glfwCreateWindow(width, height, title, MemoryUtil.NULL, MemoryUtil.NULL)
    if windowHandle == MemoryUtil.NULL then {
      throw new RuntimeException("Failed to create GLFW window")
    }

    // Key call back for closing the window
    glfwSetKeyCallback(
      windowHandle,
      this,
    )

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

  override def invoke(window: Long, key: Int, scancode: Int, action: Int, mods: Int): Unit = {
    if key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE then {
      glfwSetWindowShouldClose(windowHandle, true)
    }
    eventListeners.foreach((f) => f.invoke(window, key, scancode, action, mods))
  }

  def swapBuffers() = {
    glfwSwapBuffers(windowHandle)
  }

  def addEventListener(listener: GLFWKeyCallbackI) = {
    eventListeners += listener
  }

  def shouldClose() = {
    glfwWindowShouldClose(windowHandle)
  }

  def pollEvents() = {
    glfwPollEvents()
  }

  def destroyWindow() = {
    // Destroy the window
    Callbacks.glfwFreeCallbacks(windowHandle)
    glfwDestroyWindow(windowHandle)
    // Terminate GLFW
    glfwTerminate()
    glfwSetErrorCallback(null).free()
  }
}
