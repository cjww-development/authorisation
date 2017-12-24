import com.typesafe.config.ConfigFactory
import scala.util.{Try, Success, Failure}

val libraryName = "authorisation"

val btVersion: String = Try(ConfigFactory.load.getString("version")) match {
  case Success(ver) => ver
  case Failure(_)   => "0.1.0"
}

val dependencies: Seq[ModuleID] = Seq(
  "com.cjww-dev.libs"      %% "http-verbs"              % "2.10.0",
  "com.cjww-dev.libs"      %% "data-security"           % "2.11.0",
  "com.cjww-dev.libs"      %% "application-utilities"   % "2.10.0",
  "com.typesafe.play"      %  "play_2.11"               % "2.5.16",
  "org.scalatestplus.play" % "scalatestplus-play_2.11"  % "2.0.1"  % Test,
  "com.github.tomakehurst" % "wiremock"                 % "2.8.0"  % Test
)

lazy val library = Project(libraryName, file("."))
  .settings(
    version                              :=  btVersion,
    scalaVersion                         :=  "2.11.12",
    organization                         :=  "com.cjww-dev.libs",
    resolvers                            +=  "cjww-dev" at "http://dl.bintray.com/cjww-development/releases",
    libraryDependencies                  ++= dependencies,
    bintrayOrganization                  :=  Some("cjww-development"),
    bintrayReleaseOnPublish in ThisBuild :=  true,
    bintrayRepository                    :=  "releases",
    bintrayOmitLicense                   :=  true
  )
