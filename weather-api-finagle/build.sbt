name         := "weather-api-finagle"
version      := "0.0.1"

scalaVersion := "2.11.7"
scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  val finagleV = "6.25.0"
  Seq(
    "com.twitter"       %% "finagle-core"  % finagleV,
    "com.twitter"       %% "finagle-http"  % finagleV,
    "com.typesafe"       % "config"        % "1.2.1",
    "io.spray"           % "spray-caching" % "1.2.1",
    "com.typesafe.play" %% "play-json"     % "2.4.2"
  )
}

com.github.retronym.SbtOneJar.oneJarSettings

Revolver.settings
