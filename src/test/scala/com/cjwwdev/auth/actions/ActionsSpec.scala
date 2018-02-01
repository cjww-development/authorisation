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

package com.cjwwdev.auth.actions

import com.cjwwdev.auth.connectors.AuthConnector
import com.cjwwdev.auth.models.{AuthContext, User}
import org.joda.time.{DateTime, DateTimeZone}
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.mvc.{Action, AnyContent, Call, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import org.mockito.Mockito.when
import org.mockito.ArgumentMatchers

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class ActionsSpec extends PlaySpec with MockitoSugar {

  val mockAuthConnector = mock[AuthConnector]

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

  class Setup extends Actions with Results {
    override val authConnector = mockAuthConnector

    val testUnauthorisedCall = Call("GET", "/test/uri")

    def testUserAction(id: String): Action[AnyContent] = authorised(testUnauthorisedCall).async {
      implicit user =>
        implicit request =>
          Future(Ok(id))
    }
  }

  "testAction" should {
    "return an ok" when {
      "the user has been authorised" in new Setup {
        val request = FakeRequest()

        when(mockAuthConnector.getContext(ArgumentMatchers.any()))
          .thenReturn(Future(Some(testContext)))

        val result = testUserAction("testId")(request)
        status(result) mustBe OK
        contentAsString(result) mustBe "testId"
      }
    }

    "redirect the user to specified unauthorisedCall" when {
      "the user is not authorised" in new Setup {
        val request = FakeRequest()

        when(mockAuthConnector.getContext(ArgumentMatchers.any()))
          .thenReturn(Future(None))

        val result = testUserAction("testId")(request)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("/test/uri")
      }
    }
  }
}
