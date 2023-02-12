ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.1.3"

lazy val root = (project in file("."))
  .settings(
    name := "Visualization",
  )

val lwjglVersion = "3.3.1"
val lwjglNatives = "natives-linux"
val scalatestVersion = "3.2.15"

libraryDependencies ++= Seq(
  // LWJGL
  "org.lwjgl" % "lwjgl" % lwjglVersion,
  "org.lwjgl" % "lwjgl-glfw" % lwjglVersion,
  "org.lwjgl" % "lwjgl-opengl" % lwjglVersion,
  // LWJGL Classifiers
  "org.lwjgl" % "lwjgl" % "3.3.1" classifier lwjglNatives,
  "org.lwjgl" % "lwjgl-glfw" % "3.3.1" classifier lwjglNatives,
  "org.lwjgl" % "lwjgl-opengl" % "3.3.1" classifier lwjglNatives,
  // Scalatest
  "org.scalatest" %% "scalatest-flatspec" % scalatestVersion % Test,
  "org.scalatest" %% "scalatest-shouldmatchers" % scalatestVersion % Test,
)
