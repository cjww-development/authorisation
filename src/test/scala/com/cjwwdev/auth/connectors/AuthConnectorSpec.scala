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

import com.cjwwdev.auth.models.{AuthContext, User}
import com.cjwwdev.http.exceptions.NotFoundException
import com.cjwwdev.http.verbs.Http
import com.cjwwdev.security.encryption.DataSecurity
import org.joda.time.{DateTime, DateTimeZone}
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers
import play.api.libs.ws.WSResponse
import play.api.test.FakeRequest
import play.api.http.Status.{OK, NOT_FOUND}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

class AuthConnectorSpec extends PlaySpec with MockitoSugar with OneAppPerSuite {

  val mockHttp = mock[Http]

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

  val mockResponse = {
    val m = mock[WSResponse]
    when(m.status).thenReturn(OK)
    when(m.body).thenReturn(DataSecurity.encryptType[AuthContext](testContext))
    m
  }

  val mockFailedResponse = {
    val m = mock[WSResponse]
    when(m.status).thenReturn(NOT_FOUND)
    m
  }

  class Setup {
    val testConnector = new AuthConnector(mockHttp)
  }

  "getContext" should {
    "return an auth context" in new Setup {
      implicit val request = FakeRequest().withSession("contextId" -> "testCID")

      when(mockHttp.GET(ArgumentMatchers.any())(ArgumentMatchers.any()))
        .thenReturn(Future.successful(mockResponse))

      val result = Await.result(testConnector.getContext, 5.seconds)
      result mustBe Some(testContext)
    }

    "return none" in new Setup {
      implicit val request = FakeRequest().withSession("contextId" -> "testCID")

      when(mockHttp.GET(ArgumentMatchers.any())(ArgumentMatchers.any()))
        .thenReturn(Future.successful(mockFailedResponse))

      val result = Await.result(testConnector.getContext, 5.seconds)
      result mustBe None
    }
  }
}
