/*
 * Copyright 2018 CJWW Development
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.typesafe.config.ConfigFactory
import scala.util.{Try, Success, Failure}

val libraryName = "authorisation"

val btVersion: String = Try(ConfigFactory.load.getString("version")) match {
  case Success(ver) => ver
  case Failure(_)   => "0.1.0"
}

val dependencies: Seq[ModuleID] = Seq(
  "com.cjww-dev.libs" %% "http-verbs"            % "3.1.0",
  "com.cjww-dev.libs" %% "application-utilities" % "4.1.0",
  "com.typesafe.play" %  "play_2.12"             % "2.6.15",
  "com.cjww-dev.libs" %% "testing-framework"     % "3.2.0"   % Test
)

lazy val library = Project(libraryName, file("."))
  .settings(
    version                              :=  btVersion,
    scalaVersion                         :=  "2.12.6",
    organization                         :=  "com.cjww-dev.libs",
    resolvers                            +=  "cjww-dev" at "http://dl.bintray.com/cjww-development/releases",
    libraryDependencies                  ++= dependencies,
    bintrayOrganization                  :=  Some("cjww-development"),
    bintrayReleaseOnPublish in ThisBuild :=  true,
    bintrayRepository                    :=  "releases",
    bintrayOmitLicense                   :=  true,
    fork                    in Test      :=  true,
    javaOptions             in Test      :=  Seq(
      "-Ddata-security.key=testKey",
      "-Ddata-security.salt=testSalt",
      "-Dmicroservice.allowedApps=M0VbBTxNa0CoJXhZMlcPpjD5iPpLluq88muJVq2nhKdQ-519MpWt1vTeZmMYCUk4Ewfx2_XXY27txe0Om2kSYHr4ZBq246ngTlhPhWQIIHNNTNAF7MHOKbxxxF-gHbT-W9Q4kf6efdmRpgD6AJB-Mg"
    )
  )
