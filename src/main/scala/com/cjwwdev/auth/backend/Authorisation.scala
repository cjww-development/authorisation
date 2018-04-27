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
import play.api.mvc.Results.Forbidden
import play.api.http.Status.FORBIDDEN
import play.api.mvc.{Request, Result}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait Authorisation extends BaseAuth {
  val authConnector: AuthConnector

  protected def authorised(id: String)(f: CurrentUser => Future[Result])(implicit request: Request[_]): Future[Result] = {
    authConnector.getCurrentUser flatMap { context =>
      mapToAuthResult(id, context) match {
        case Authorised(ac) => f(ac)
        case _              => withFutureJsonResponseBody(FORBIDDEN, "The user is not authorised to access this resource") { json =>
          Future(Forbidden(json))
        }
      }
    }
  }

  private def mapToAuthResult(id: String, currentUser: Option[CurrentUser])(implicit request: Request[_]): AuthorisationResult = {
    validateAppId match {
      case Authenticated => currentUser.fold(notAuthorised)(ac => if(id == ac.id) authorised(ac) else notAuthorised)
      case _             => notAuthorised
    }
  }

  private def authorised(currentUser: CurrentUser): AuthorisationResult = {
    logger.info(s"[mapToAuthResult]: User authorised as ${currentUser.id}")
    Authorised(currentUser)
  }

  private def notAuthorised: AuthorisationResult = {
    logger.warn("[mapToAuthResult]: User not authorised action deemed forbidden")
    NotAuthorised
  }
}
