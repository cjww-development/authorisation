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

package com.cjwwdev.auth.backend

import com.cjwwdev.auth.connectors.AuthConnector
import com.cjwwdev.auth.models.CurrentUser
import com.cjwwdev.http.headers.HeaderPackage
import com.cjwwdev.implicits.ImplicitDataSecurity._
import com.cjwwdev.testing.unit.UnitTestSpec
import org.joda.time.{DateTime, DateTimeZone}
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import play.api.mvc.Results.Ok
import play.api.mvc.{Request, Result}
import play.api.test.FakeRequest

import scala.concurrent.Future

class AuthenticationSpec extends UnitTestSpec {

  val mockAuthConnector = mock[AuthConnector]

  class Setup extends Authentication {
    override val authConnector = mockAuthConnector

    def testAuthentication(implicit request: Request[_]): Future[Result] = authenticated("testUserId") {
      Future.successful(Ok)
    }
  }

  val now = new DateTime(DateTimeZone.UTC)

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

  "authenticated" should {
    "return an Ok" when {
      "a matching auth context has been found" in new Setup {
        implicit val request = FakeRequest()
          .withHeaders("cjww-headers" -> HeaderPackage("testSessionStoreId", "testCookieId").encryptType)

        when(mockAuthConnector.getCurrentUser(ArgumentMatchers.any()))
          .thenReturn(Future.successful(Some(testUser)))

        val result = testAuthentication
        status(result) mustBe OK
      }
    }

    "return a forbidden" when {
      "no auth context has been found" in new Setup {
        implicit val request = FakeRequest()
          .withHeaders("cjww-headers" -> HeaderPackage("testSessionStoreId", "testCookieId").encryptType)

        when(mockAuthConnector.getCurrentUser(ArgumentMatchers.any()))
          .thenReturn(Future.successful(None))

        val result = testAuthentication
        status(result) mustBe FORBIDDEN
      }
    }
  }
}
