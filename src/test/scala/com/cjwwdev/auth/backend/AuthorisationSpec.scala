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

class AuthorisationSpec extends UnitTestSpec {

  val mockAuthConnector = mock[AuthConnector]

  class Setup extends Authorisation {
    override val appId: String = "testAppId"
    override val authConnector = mockAuthConnector

    def testAuthorisation(id: String)(implicit request: Request[_]): Future[Result] = authorised(id) { _ =>
      Future.successful(Ok("testUserId"))
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

  "authorised" should {
    "return an Ok" when {
      "an AuthContext has been found and the user id's match" in new Setup {
        implicit val request = FakeRequest()
          .withHeaders("cjww-headers" -> HeaderPackage("testSessionStoreId", Some("testCookieId")).encrypt)

        when(mockAuthConnector.getCurrentUser(ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(Some(testUser)))

        val result = testAuthorisation("testUserId")

        status(result) mustBe OK
        contentAsString(result) mustBe "testUserId"
      }
    }

    "return a forbidden" when {
      "no AuthContext has been found" in new Setup {
        implicit val request = FakeRequest()
          .withHeaders("cjww-headers" -> HeaderPackage("testSessionStoreId", Some("testCookieId")).encrypt)

        when(mockAuthConnector.getCurrentUser(ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(None))

        val result = testAuthorisation("testMismatchId")

        status(result) mustBe FORBIDDEN
      }

      "the users id don't match" in new Setup {
        implicit val request = FakeRequest()
          .withHeaders("cjww-headers" -> HeaderPackage("testSessionStoreId", Some("testCookieId")).encrypt)

        when(mockAuthConnector.getCurrentUser(ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(Some(testUser)))

        val result = testAuthorisation("testMismatchId")

        status(result) mustBe FORBIDDEN
      }
    }
  }
}
