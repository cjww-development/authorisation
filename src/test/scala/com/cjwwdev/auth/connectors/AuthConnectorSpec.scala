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

package com.cjwwdev.auth.connectors

import com.cjwwdev.auth.helpers.MockHttpResponse
import com.cjwwdev.auth.models.CurrentUser
import com.cjwwdev.http.headers.HeaderPackage
import com.cjwwdev.http.verbs.Http
import com.cjwwdev.implicits.ImplicitDataSecurity._
import com.cjwwdev.security.obfuscation.Obfuscation._
import com.cjwwdev.security.obfuscation.{Obfuscation, Obfuscator}
import com.cjwwdev.testing.unit.UnitTestSpec
import org.joda.time.{DateTime, DateTimeZone, LocalDateTime}
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import com.cjwwdev.http.responses.EvaluateResponse.{SuccessResponse, ErrorResponse}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class AuthConnectorSpec extends UnitTestSpec with MockHttpResponse {

  final val now = new DateTime(DateTimeZone.UTC)

  val testUser = CurrentUser(
    contextId      = "testContextId",
    id             = "testUserId",
    credentialType = "testTyoe",
    orgDeversityId = Some("testOrgDevId"),
    orgName        = None,
    firstName      = None,
    lastName       = None,
    role           = None,
    enrolments     = None
  )

  val mockHttp = mock[Http]

  val testConnector = new AuthConnector {
    override val http                                       = mockHttp
    override val authUrl: String                            = "/test/auth-microservice"
    override def authUri(cookieId: String)                  = "/test"
    override val sessionStoreUrl: String                    = "/test/session-store"
    override def sessionStoreUri(contextId: String): String = "/test"
  }

  val testSessionId = "testSessionId".encrypt

  def testApiResponse(statusCode: Int, json: String): JsObject = Json.obj(
    "uri"       -> "/test/uri",
    "method"    -> "GET",
    "status"    -> statusCode,
    "sessionId" -> "testSessionId",
    "body"      -> json,
    "stats"     -> Json.obj(
      "requestCompletedAt" -> s"${LocalDateTime.now}"
    )
  )

  case class Context(contextId: String, other: Option[String] = None)
  implicit val format = Json.format[Context]
  implicit val obfuscator: Obfuscator[Context] = new Obfuscator[Context] {
    override def encrypt(value: Context): String = Obfuscation.obfuscateJson(Json.toJson(value))
  }

  "getCurrentUser" should {
    "return an auth context" in {
      implicit val request = FakeRequest().withSession("cookieId" -> "testSessionId")

      when(mockHttp.constructHeaderPackageFromRequestHeaders(ArgumentMatchers.eq(request)))
        .thenReturn(Some(HeaderPackage("testSessionStoreId", Some("testCookieId"))))

      when(mockHttp.get(ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.eq(request)))
        .thenReturn(
          Future.successful(SuccessResponse(mockWSResponse(OK, testApiResponse(OK, testSessionId.encrypt)))),
          Future.successful(SuccessResponse(mockWSResponse(OK, testApiResponse(OK, testUser.encrypt))))
        )

      val result = Await.result(testConnector.getCurrentUser, 5.seconds)
      result mustBe Some(testUser)
    }

    "return none" when {
      "no matching AuthContext was found" in {
        implicit val request = FakeRequest().withSession("contextId" -> "testCID")

        when(mockHttp.constructHeaderPackageFromRequestHeaders(ArgumentMatchers.eq(request)))
          .thenReturn(Some(HeaderPackage("testSessionStoreId", Some("testCookieId"))))

        when(mockHttp.get(ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.eq(request)))
          .thenReturn(
            Future.successful(SuccessResponse(mockWSResponse(OK, testApiResponse(OK, "testContextId".encrypt)))),
            Future.successful(ErrorResponse(mockWSResponse(NOT_FOUND, testApiResponse(NOT_FOUND, ""))))
          )

        val result = Await.result(testConnector.getCurrentUser, 5.seconds)
        result mustBe None
      }

      "no matching Session was found, but a sessionId was pulled from the header" in {
        implicit val request = FakeRequest()

        when(mockHttp.constructHeaderPackageFromRequestHeaders(ArgumentMatchers.eq(request)))
          .thenReturn(Some(HeaderPackage("testSessionStoreId", Some("testSessionId"))))

        when(mockHttp.get(ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.eq(request)))
          .thenReturn(Future.successful(ErrorResponse(mockWSResponse(NOT_FOUND, testApiResponse(NOT_FOUND, "")))))

        val result = Await.result(testConnector.getCurrentUser, 5.seconds)
        result mustBe None
      }

      "no matching Session was found" in {
        implicit val request = FakeRequest()

        when(mockHttp.constructHeaderPackageFromRequestHeaders(ArgumentMatchers.eq(request)))
          .thenReturn(Some(HeaderPackage("testSessionStoreId", None)))

        when(mockHttp.get(ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.eq(request)))
          .thenReturn(Future.successful(ErrorResponse(mockWSResponse(NOT_FOUND, testApiResponse(NOT_FOUND, "")))))

        val result = Await.result(testConnector.getCurrentUser, 5.seconds)
        result mustBe None
      }

      "no HeaderPackage was found in the request" in {
        implicit val request = FakeRequest().withSession("contextId" -> "testCID")

        when(mockHttp.constructHeaderPackageFromRequestHeaders(ArgumentMatchers.eq(request)))
          .thenReturn(None)

        val result = Await.result(testConnector.getCurrentUser, 5.seconds)
        result mustBe None
      }
    }
  }
}
