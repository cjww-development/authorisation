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
import com.cjwwdev.logging.Logging
import com.cjwwdev.responses.ApiResponse
import play.api.mvc.Results.Forbidden
import play.api.http.Status.FORBIDDEN
import play.api.mvc.{Request, Result}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait Authentication extends BaseAuth with Logging with ApiResponse {
  val authConnector: AuthConnector

  protected def authenticated(id: String)(f: => Future[Result])(implicit request: Request[_]): Future[Result] = {
    authConnector.getCurrentUser flatMap { context =>
      mapToAuthResult(id, context) match {
        case Authenticated  => f
        case _              => withFutureJsonResponseBody(FORBIDDEN, "The user could not be authenticated") { json =>
          Future(Forbidden(json))
        }
      }
    }
  }

  private def mapToAuthResult(id: String, currentUser: Option[CurrentUser])(implicit request: Request[_]): AuthorisationResult = {
    validateAppId match {
      case Authenticated => currentUser.fold(notAuthorised)(currentUser => authorised(currentUser.id))
      case _             => notAuthorised
    }
  }

  private def authorised(id: String): AuthorisationResult = {
    logger.info(s"[mapToAuthResult]: User authorised as $id")
    Authenticated
  }

  private def notAuthorised: AuthorisationResult = {
    logger.warn("[mapToAuthResult]: User not authorised action deemed forbidden")
    NotAuthorised
  }
}
