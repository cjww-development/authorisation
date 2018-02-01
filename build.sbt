/*
 *   Copyright 2018 CJWW Development
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

import com.typesafe.config.ConfigFactory
import scala.util.{Try, Success, Failure}

val libraryName = "authorisation"

val btVersion: String = Try(ConfigFactory.load.getString("version")) match {
  case Success(ver) => ver
  case Failure(_)   => "0.1.0"
}

val dependencies: Seq[ModuleID] = Seq(
  "com.cjww-dev.libs"      %% "http-verbs"              % "2.14.0",
  "com.cjww-dev.libs"      %% "data-security"           % "2.12.0",
  "com.cjww-dev.libs"      %% "application-utilities"   % "2.14.0",
  "com.typesafe.play"      %  "play_2.11"               % "2.5.16",
  "org.scalatestplus.play" %  "scalatestplus-play_2.11" % "2.0.1"  % Test,
  "com.github.tomakehurst" %  "wiremock"                % "2.8.0"  % Test,
  "org.mockito"            %  "mockito-core"            % "2.13.0" % Test
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
    bintrayOmitLicense                   :=  true,
    fork                    in Test      :=  true,
    javaOptions             in Test      :=  Seq(
      "-Dmicroservice.data-security.key=testKey",
      "-Dmicroservice.data-security.salt=testSalt",
      "-Dmicroservice.external-services.deversity-frontend.application-id=testDevFEId",
      "-Dmicroservice.external-services.deversity.application-id=testDevId",
      "-Dmicroservice.external-services.diagnostics-frontend.application-id=testDiagFEId",
      "-Dmicroservice.external-services.hub-frontend.application-id=testHubFEId",
      "-Dmicroservice.external-services.auth-service.application-id=testAuthFEId",
      "-Dmicroservice.external-services.auth-microservice.application-id=testAuthBeId",
      "-Dmicroservice.external-services.accounts-microservice.application-id=testAccBeId",
      "-Dmicroservice.external-services.session-store.application-id=testSessionStoreId"
    )
  )
