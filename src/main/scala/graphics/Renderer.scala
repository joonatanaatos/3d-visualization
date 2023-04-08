package graphics

import game.{Game, GameState}
import logic.{Cube, Demon, GameObject, Stage, Wall, World}
import org.lwjgl.opengl.GL11.glViewport
import graphics.Utils.glCheck
import org.joml.{Matrix3f, Matrix4f, Vector2f, Vector3f}

/**
 * Renderer draws the World onto the screen
 * @param game
 *   Game to be visualized
 * @param window
 *   Window that the World should be rendered onto
 */
class Renderer(val game: Game, val window: Window) {
  private val world = game.world
  private val menu = game.menu

  private val renderDistance = 40

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
    Matrix4f().scaleXY(width, height).scale(0.5f).translate(0f, 1f, 0f)
  private def cubeSideShapeMatrix(xAngle: Float, yAngle: Float, size: Float, rotation: Vector3f) =
    Matrix4f()
      .rotateZ(rotation.z)
      .rotateY(rotation.y)
      .rotateX(rotation.x)
      .rotateY(yAngle)
      .rotateX(xAngle)
      .scale(size / 2f)
      .translate(Vector3f(0f, 0f, 1f))

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

    val (scareTimer, scareCause) = world.getScareStatus
    if scareTimer != 0 && scareCause.isDefined then drawScare(scareTimer, scareCause.get)
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
      wallPos.z < 1 && wallPos.length < renderDistance
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
    if gameObject.getPosition.sub(world.player.getPosition).length < renderDistance then {
      gameObject match {
        case demon: Demon => drawDemon(demon)
        case cube: Cube   => drawCube(cube)
        case _            =>
      }
    }
  }

  private def drawDemon(demon: Demon): Unit = {
    val demonPos = demon.getPosition
    val angle = demon.getDirection
    val normal = Vector3f(0f, 0f, 1f).rotateY(angle)

    demonPos.sub(cameraPosition)

    val modelMatrix =
      createModelMatrix(
        demonPos,
        angle,
        creatureShapeMatrix(demonTexture.getAspectRatio * demon.height, demon.height),
      )

    renderingHelper.drawTexture3D(
      modelMatrix,
      cameraDirection,
      demonTexture,
      normal,
      viewChange,
    )
  }

  private def drawCube(cube: Cube): Unit = {
    val cubePos = cube.getPosition
    val rotation = cube.getRotation
    val size = cube.size

    cubePos.sub(cameraPosition)

    // [normal vector, color]
    val surfaces = Array(
      (Vector3f(1f, 0, 0), Array(0, 0, 1f, 1f)),
      (Vector3f(-1f, 0, 0), Array(1f, 1f, 0, 1f)),
      (Vector3f(0, 1f, 0), Array(0, 1f, 0, 1f)),
      (Vector3f(0, -1f, 0), Array(1f, 0, 1f, 1f)),
      (Vector3f(0, 0, 1f), Array(1f, 0, 0, 1f)),
      (Vector3f(0, 0, -1f), Array(0, 1f, 1f, 1f)),
    )

    for (surface <- surfaces) {
      val normal = surface(0)

      val yAngle =
        if surface(0).z == -1f then math.Pi.toFloat else normal.x * math.Pi.toFloat / 2f
      val xAngle = -normal.y * math.Pi.toFloat / 2f

      val position = Vector3f(cubePos)
      val shapeMatrix = cubeSideShapeMatrix(xAngle, yAngle, size, rotation)
      normal.rotateX(rotation.x).rotateY(rotation.y).rotateZ(rotation.z)

      val modelMatrix = createModelMatrix(
        position,
        0,
        shapeMatrix,
      )

      renderingHelper.drawColor3D(
        modelMatrix,
        cameraDirection,
        surface(1),
        normal,
        viewChange,
      )
    }
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

  private def drawScare(scareTimer: Int, demon: Demon): Unit = {
    val modelMatrix = Matrix4f()
    modelMatrix.translate(0f, -0.2f, 0f)
    modelMatrix.scale(2f - (scareTimer / 200f))
    modelMatrix.scaleXY(
      2f * demon.height * demonTexture.getAspectRatio / window.getAspectRatio,
      demon.height,
    )
    val shake = (0 until 2).map(_ => (math.random().toFloat - 0.5f) * 0.4f)

    renderingHelper.drawTexture2D(modelMatrix, demonTexture, Vector2f(shake(0), shake(1)), true)
  }

  private def updateViewport(): Unit = {
    if (window.wasResized()) {
      glCheck { glViewport(0, 0, window.width, window.height) }
    }
  }

  private def updateLighting(): Unit = {
    renderingHelper.setAmbientLightBrightness(0.12f)
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
