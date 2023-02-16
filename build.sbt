ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.1.3"

lazy val root = (project in file("."))
  .settings(
    name := "Visualization",
  )

val lwjglVersion = "3.3.1"
val jomlVersion = "1.10.5"
val scalatestVersion = "3.2.15"

val lwjglNatives = System.getProperty("os.name") match {
  case n if n.startsWith("Linux")   => "natives-linux"
  case n if n.startsWith("Mac")     => "natives-macos"
  case n if n.startsWith("Windows") => "natives-windows"
  case _                            => throw new Exception("Unknown platform!")
}

libraryDependencies ++= Seq(
  // LWJGL
  "org.lwjgl" % "lwjgl" % lwjglVersion,
  "org.lwjgl" % "lwjgl-glfw" % lwjglVersion,
  "org.lwjgl" % "lwjgl-opengl" % lwjglVersion,
  // LWJGL Classifiers
  "org.lwjgl" % "lwjgl" % lwjglVersion classifier lwjglNatives,
  "org.lwjgl" % "lwjgl-glfw" % lwjglVersion classifier lwjglNatives,
  "org.lwjgl" % "lwjgl-opengl" % lwjglVersion classifier lwjglNatives,
  // Java OpenGL Math Library
  "org.joml" % "joml" % jomlVersion,
  // Scalatest
  "org.scalatest" %% "scalatest-flatspec" % scalatestVersion % Test,
  "org.scalatest" %% "scalatest-shouldmatchers" % scalatestVersion % Test,
)
