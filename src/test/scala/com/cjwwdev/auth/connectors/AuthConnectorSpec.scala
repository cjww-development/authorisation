// Copyright (C) 2016-2017 the original author or authors.
// See the LICENCE.txt file distributed with this work for additional
// information regarding copyright ownership.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.cjwwdev.auth.connectors

import com.cjwwdev.auth.helpers.WireMockHelper
import com.cjwwdev.auth.models.{AuthContext, User}
import com.cjwwdev.config.{ConfigurationLoader, ConfigurationLoaderImpl}
import com.cjwwdev.http.exceptions.NotFoundException
import com.cjwwdev.http.verbs.{Http, HttpImpl}
import com.cjwwdev.security.encryption.DataSecurity
import org.joda.time.{DateTime, DateTimeZone}
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.WSClient
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

class AuthConnectorSpec extends WireMockHelper {

  final val now = new DateTime(DateTimeZone.UTC)

  val testContext = AuthContext(
    "testCID",
    User(
      "testUID",
      Some("testFirstName"),
      Some("testLastName"),
      None,
      "individual",
      Some("student")
    ),
    "/test/uri",
    "/test/uri",
    "/test/uri",
    now
  )

  val config   = app.injector.instanceOf(classOf[ConfigurationLoaderImpl])
  val wsClient = app.injector.instanceOf(classOf[WSClient])
  val http     = new HttpImpl(wsClient, config)

  val testConnector = new AuthConnectorImpl(http, config)

  "getContext" should {
    "return an auth context" in {
      implicit val request = FakeRequest().withSession("contextId" -> "testCID")

      wmGet("/session-store/session/invalid-cookie/context", OK, DataSecurity.encryptType[JsValue](Json.parse("""{"contextId"  : "testContextId"}""")))

      wmGet("/auth/get-context/testContextId", OK, DataSecurity.encryptType[AuthContext](testContext))

      val result = Await.result(testConnector.getContext, 5.seconds)
      result mustBe Some(testContext)
    }

    "return none" in {
      implicit val request = FakeRequest().withSession("contextId" -> "testCID")

      wmGet("/session-store/session/invalid-cookie/context", OK, DataSecurity.encryptType[JsValue](Json.parse("""{"contextId"  : "testContextId"}""")))

      wmGet("/auth/get-context/testContextId", NOT_FOUND, "")

      val result = Await.result(testConnector.getContext, 5.seconds)
      result mustBe None
    }
  }
}
