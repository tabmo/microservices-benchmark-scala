name := """weather-api-play"""

version := "0.0.1"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(RestModule)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  ws,
  specs2 % Test
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

routesGenerator := InjectedRoutesGenerator

// Scala settings
scalacOptions ++= Seq(
  "-deprecation",           // Warn when deprecated API are used
  "-feature",               // Warn for usages of features that should be importer explicitly
  "-unchecked",             // Warn when generated code depends on assumptions
  "-Ywarn-dead-code",       // Warn when dead code is identified
  "-Ywarn-numeric-widen",   // Warn when numeric are widened
  "-Xlint",                 // Additional warnings (see scalac -Xlint:help)
  "-Ywarn-adapted-args"     // Warn if an argument list is modified to match the receive
)

/*
 * Rest-specific configuration to override play2 layout
 * `assets` and `public` folder will not be generated
 */
lazy val RestModule = {
  val blackhole = new java.io.File("/dev/null")
  Seq(
    sourceDirectory in Assets  := blackhole,
    sourceDirectory in TestAssets := blackhole,
    resourceDirectory in Assets := blackhole
  )
}