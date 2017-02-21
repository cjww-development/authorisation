import com.typesafe.config.ConfigFactory
import scala.util.{Try, Success, Failure}

val btVersion: String = {
  Try(ConfigFactory.load.getString("version")) match {
    case Success(ver) => ver
    case Failure(_) => "INVALID_RELEASE_VERSION"
  }
}

name := "authorisation"
version := btVersion
scalaVersion := "2.11.8"
organization := "com.cjww-dev.libs"

val cjwwDep: Seq[ModuleID] = Seq(
  "com.cjww-dev.libs" %% "http-verbs" % "0.2.0",
  "com.cjww-dev.libs" %% "logging" % "0.1.0"
)
val codeDep: Seq[ModuleID] = Seq("com.typesafe.play" % "play_2.11" % "2.5.12")
val testDep: Seq[ModuleID] = Seq(
  "org.scalatestplus.play" % "scalatestplus-play_2.11" % "1.5.1",
  "org.mockito" % "mockito-core" % "2.2.29"
)

libraryDependencies ++= cjwwDep
libraryDependencies ++= codeDep
libraryDependencies ++= testDep

resolvers += "cjww-dev" at "http://dl.bintray.com/cjww-development/releases"

bintrayOrganization := Some("cjww-development")
bintrayReleaseOnPublish in ThisBuild := false
bintrayRepository := "releases"
bintrayOmitLicense := true
    