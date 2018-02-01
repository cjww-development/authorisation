/*
 *    Copyright 2018 CJWW Development
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package com.cjwwdev.auth.connectors

import com.cjwwdev.auth.helpers.MockHttpResponse
import com.cjwwdev.auth.models.{AuthContext, User}
import com.cjwwdev.http.exceptions.NotFoundException
import com.cjwwdev.http.headers.HeaderPackage
import com.cjwwdev.http.verbs.Http
import org.joda.time.{DateTime, DateTimeZone}
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.FakeRequest

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class AuthConnectorSpec extends PlaySpec with MockitoSugar with MockHttpResponse with GuiceOneAppPerSuite {

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

  val mockHttp = mock[Http]

  val testConnector = new AuthConnector {
    override val http             = mockHttp
    override val authMicroservice = "/test/auth-microservice"
    override val sessionStore     = "/test/session-store"
  }

  "getContext" should {
    "return an auth context" in {
      implicit val request = FakeRequest().withSession("cookieId" -> "testSessionId")

      when(mockHttp.constructHeaderPackageFromRequestHeaders(ArgumentMatchers.eq(request)))
        .thenReturn(Some(HeaderPackage("testSessionStoreId", "testCookieId")))

      when(mockHttp.GET(ArgumentMatchers.any())(ArgumentMatchers.eq(request)))
        .thenReturn(
          Future.successful(mockWSResponseWithString(200, "testContextId")),
          Future.successful(mockWSResponse[AuthContext](200, testContext))
        )

      val result = Await.result(testConnector.getContext, 5.seconds)
      result mustBe Some(testContext)
    }

    "return none" when {
      "no matching AuthContext was found" in {
        implicit val request = FakeRequest().withSession("contextId" -> "testCID")

        when(mockHttp.constructHeaderPackageFromRequestHeaders(ArgumentMatchers.eq(request)))
          .thenReturn(Some(HeaderPackage("testSessionStoreId", "testCookieId")))

        when(mockHttp.GET(ArgumentMatchers.any())(ArgumentMatchers.eq(request)))
          .thenReturn(
            Future.successful(mockWSResponseWithString(200, "testContextId")),
            Future.failed(new NotFoundException("test message"))
          )

        val result = Await.result(testConnector.getContext, 5.seconds)
        result mustBe None
      }

      "no HeaderPackage was found in the request" in {
        implicit val request = FakeRequest().withSession("contextId" -> "testCID")

        when(mockHttp.constructHeaderPackageFromRequestHeaders(ArgumentMatchers.eq(request)))
          .thenReturn(None)

        val result = Await.result(testConnector.getContext, 5.seconds)
        result mustBe None
      }
    }
  }
}
