package graphics

import game.{Game, GameState}
import logic.{Demon, GameObject, Stage, Wall, World}
import org.lwjgl.opengl.GL11.glViewport
import graphics.Utils.glCheck
import org.joml.{Matrix3f, Matrix4f, Vector2f, Vector3f}

/**
 * Renderer draws the World onto the screen
 * @param world
 *   World to be visualized
 * @param window
 *   Window that the World should be rendered onto
 */
class Renderer(val game: Game, val window: Window) {
  private val world = game.world
  private val menu = game.menu

  private val renderingHelper = RenderingHelper(window)
  private val cameraRelativeToPlayer = Vector3f(0f, 0.4f, 0f)
  private var cameraPosition = Vector3f(cameraRelativeToPlayer)
  private var cameraDirection = Vector2f(0f, 0f)
  private var viewChange = Vector2f(0f, 0f)

  private val wallHeight = world.wallHeight
  private val wallShapeMatrix =
    Matrix4f().scaleXY(1f, wallHeight).scale(0.5f).translate(0f, 1f, 0f)
  private val floorShapeMatrix =
    Matrix4f().scale(0.5f).translate(1f, 0f, 1f).rotateX(math.Pi.toFloat / 2f)
  private def creatureShapeMatrix(width: Float, height: Float) =
    Matrix4f().scaleXY(width, 1f).scale(0.5f * height).translate(0f, 1f, 0f)

  private val wallTexture = Texture("wall")
  private val floorTexture = Texture("floor")
  private val demonTexture = Texture("demon")
  private val nameTexture = Texture("text/name")
  private val startTexture = Texture("text/start")

  def render(state: GameState): Unit = {
    updateViewport()
    renderingHelper.clear()
    state match {
      case GameState.Game       => renderGame()
      case GameState.Menu       => renderMenu()
      case GameState.MenuToGame => renderTransition()
    }
  }

  private def renderGame(): Unit = {
    updateCameraPosition()
    updateLighting()
    viewChange = getCameraShake

    // Begin draw
    drawFloorAndCeiling(world.stage.width, world.stage.height)
    visibleWalls().foreach(drawWall)
    world.getGameObjects.foreach(drawGameObject)

    val scareTimer = world.getScareStatus
    if scareTimer != 0 then drawScare(scareTimer)
  }

  private def renderMenu(): Unit = {
    val fadeInOpacity = math.pow(menu.fadeInTimer.toFloat / menu.fadeInTime.toFloat, 2).toFloat

    val nameModelMatrix = Matrix4f()
    nameModelMatrix.translate(0f, 0.2f, 0f)
    nameModelMatrix
      .scale(1f, window.getAspectRatio / nameTexture.getAspectRatio, 0f)
      .scale(0.4f)
    renderingHelper.drawTexture2D(nameModelMatrix, nameTexture, opacity = fadeInOpacity)

    val startOpacity = math.pow(math.sin(menu.startTextFaze).toFloat, 2).toFloat
    val startModelMatrix = Matrix4f()
    startModelMatrix.translate(0f, -0.65f, 0f)
    startModelMatrix
      .scale(1f, window.getAspectRatio / startTexture.getAspectRatio, 0f)
      .scale(0.4f)
    renderingHelper.drawTexture2D(
      startModelMatrix,
      startTexture,
      opacity = fadeInOpacity * startOpacity,
    )
  }

  private def renderTransition(): Unit = {
    val transition = game.getTransitionProgress
    val opacity = if transition < 0.5f then {
      renderMenu()
      transition * 2f
    } else {
      renderGame()
      (1f - transition) * 2f
    }
    renderingHelper.drawColor2D(Matrix4f(), Array(0f, 0f, 0f, 1f), opacity = opacity)
  }

  private def visibleWalls(): Array[Wall] = {
    val (horizontalWalls, verticalWalls) = world.stage.getWallPositions

    val playerPos = world.player.getPosition
    val playerDir = world.player.getDirection

    def isVisible(wallOption: Option[Wall]): Boolean = {
      if wallOption.isEmpty then return false
      val wall = wallOption.get
      val wallPos = Vector3f(wall.xPos, 0f, wall.zPos)
      wallPos.sub(playerPos)
      wallPos.rotateY(playerDir.x)
      wallPos.z < 1
    }

    (horizontalWalls ++ verticalWalls).flatten.filter(isVisible).map(w => w.get)
  }

  private def createModelMatrix(
      position: Vector3f,
      rotation: Float,
      shapeMatrix: Matrix4f,
  ): Matrix4f = {
    val modelMatrix = Matrix4f()
    // 3. Translate + view bobbing
    modelMatrix.translate(position)
    // 2. Rotate
    modelMatrix.rotateY(rotation)
    // 1. Scale
    modelMatrix.mul(shapeMatrix)
    modelMatrix
  }

  private def getDistanceFromDemon: Float = {
    val playerPos = world.player.getPosition
    val demons = world.getGameObjects.filter(_.isInstanceOf[Demon]).map(_.asInstanceOf[Demon])
    if demons.isEmpty then return Float.MaxValue
    val demonPositions = demons.map(_.getPosition)
    val distances = demonPositions.map(pos => pos.sub(playerPos).length())
    distances.min
  }

  private def getDemonEffectIntensity: Float = {
    val distanceFromDemon = getDistanceFromDemon
    if distanceFromDemon < Demon.attackThreshold then {
      0.5f / (1 + distanceFromDemon * distanceFromDemon)
    } else 0f
  }

  private def getCameraShake: Vector2f = {
    val demonIntensity = getDemonEffectIntensity
    val demonShake = (0 until 2).map(_ => (math.random().toFloat - 0.5f) * demonIntensity).toArray
    val viewBobbing = math.sin(world.player.getViewChange).toFloat / 75f
    Vector2f(demonShake(0), demonShake(1) + viewBobbing)
  }

  private def drawWall(wall: Wall): Unit = {
    // Calculate wall pos
    val wallPos = Vector3f(wall.xPos.toFloat, 0f, wall.zPos.toFloat)
    wallPos.sub(cameraPosition)
    val wallAlignment =
      if wall.direction.horizontal then Vector3f(0f, 0f, 0.5f) else Vector3f(0.5f, 0f, 0f)
    wallPos.add(wallAlignment)
    // Calculate wall angle
    val angle = wall.direction.angle + math.Pi.toFloat / 2f
    val normal = Vector3f(1f, 0f, 0f).rotateY(wall.direction.angle).normalize()
    val modelMatrix = createModelMatrix(wallPos, angle, wallShapeMatrix)

    renderingHelper.drawTexture3D(
      modelMatrix,
      cameraDirection,
      wallTexture,
      normal,
      viewChange,
    )
  }

  private def drawGameObject(gameObject: GameObject): Unit = {
    gameObject match {
      case demon: Demon => drawDemon(demon)
      case _            =>
    }
  }

  private def drawDemon(demon: Demon): Unit = {
    val demonPos = demon.getPosition
    val angle = demon.getDirection
    val normal = Vector3f(0f, 0f, 1f).rotateY(angle)

    demonPos.sub(cameraPosition)

    val modelMatrix =
      createModelMatrix(demonPos, angle, creatureShapeMatrix(Demon.drawSize, demon.height))

    renderingHelper.drawTexture3D(
      modelMatrix,
      cameraDirection,
      demonTexture,
      normal,
      viewChange,
    )
  }

  private def drawFloorAndCeiling(width: Int, depth: Int): Unit = {
    val position = Vector3f()
    position.sub(cameraPosition)

    for (x <- 0 until width) {
      for (z <- 0 until depth) {
        for (y <- Array(0f, wallHeight)) {
          val modelMatrix = createModelMatrix(Vector3f(position).add(x, y, z), 0f, floorShapeMatrix)

          renderingHelper.drawTexture3D(
            modelMatrix,
            cameraDirection,
            floorTexture,
            Vector3f(0f, if y == 0f then 1f else -1f, 0f),
            viewChange,
          )
        }
      }
    }
  }

  private def drawScare(scareTimer: Int): Unit = {
    val modelMatrix = Matrix4f()
    modelMatrix.translate(0f, -0.2f, 0f)
    modelMatrix.scale(1.6f - (scareTimer / 200f))
    modelMatrix.scaleXY(Demon.drawSize, window.getAspectRatio)
    val shake = (0 until 2).map(_ => (math.random().toFloat - 0.5f) * 0.4f)

    renderingHelper.drawTexture2D(modelMatrix, demonTexture, Vector2f(shake(0), shake(1)), true)
  }

  private def updateViewport(): Unit = {
    if (window.wasResized()) {
      glCheck { glViewport(0, 0, window.width, window.height) }
    }
  }

  private def updateLighting(): Unit = {
    renderingHelper.setAmbientLightBrightness(0.02f)
    renderingHelper.setPointLights(
      world.lights
        .map(light => (light.getPosition.sub(cameraPosition), light.getBrightness))
        .sortBy(light => light(0).length())
        .take(renderingHelper.maxNumberOfLights),
    )
  }

  private def updateCameraPosition(): Unit = {
    cameraPosition = world.player.getPosition.add(cameraRelativeToPlayer)
    cameraDirection = world.player.getDirection
  }

  def destroy(): Unit = {
    renderingHelper.destroy()
  }
}
