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

import javax.inject.Inject
import com.cjwwdev.auth.models.CurrentUser
import com.cjwwdev.auth.models.CurrentUser._
import com.cjwwdev.config.ConfigurationLoader
import com.cjwwdev.http.responses.WsResponseHelpers
import com.cjwwdev.http.verbs.Http
import com.cjwwdev.http.responses.EvaluateResponse.{SuccessResponse, ErrorResponse}
import play.api.mvc.Request

import scala.concurrent.{ExecutionContext => ExC,Future}

class AuthConnectorImpl @Inject()(val http: Http, val configLoader: ConfigurationLoader) extends AuthConnector {
  val authUrl: String = configLoader.getServiceUrl("auth-microservice")
  def authUri(cookieId: String): String = {
    configLoader
      .get[String]("microservice.external-services.auth-microservice.uri")
      .replace(":sessionId", cookieId)
  }

  val sessionStoreUrl: String = configLoader.getServiceUrl("session-store")
  def sessionStoreUri(contextId: String): String = {
    configLoader
      .get[String]("microservice.external-services.session-store.uri")
      .replace(":contextId", contextId)
  }
}

trait AuthConnector extends WsResponseHelpers {
  val http: Http

  val authUrl: String
  def authUri(cookieId: String): String

  val sessionStoreUrl: String
  def sessionStoreUri(contextId: String): String

  private type User = Option[CurrentUser]

  def getCurrentUser(implicit ec: ExC, request: Request[_]): Future[User] = {
    getCookieId.fold(Future.successful(Option.empty[CurrentUser])) { cookieId =>
      consultSessionStore(cookieId) {
        consultAuth(_)
      }
    }
  }

  private def getCookieId(implicit ec: ExC, request: Request[_]): Option[String] = {
    http.constructHeaderPackageFromRequestHeaders.fold(Option.empty[String])(_.cookieId)
  }

  private def consultSessionStore(cookieId: String)(f: String => Future[User])(implicit ec: ExC, request: Request[_]): Future[User] = {
    http.get(s"$sessionStoreUrl${sessionStoreUri(cookieId)}") flatMap {
      case SuccessResponse(resp) => resp.toResponseString(needsDecrypt = true).fold(f(_), _ => Future.successful(None))
      case ErrorResponse(_)      => Future.successful(None)
    }
  }

  private def consultAuth(contextId: String)(implicit ec: ExC, request: Request[_]): Future[User] = {
    http.get(s"$authUrl${authUri(contextId)}") map {
      case SuccessResponse(resp) => resp.toDataType[CurrentUser](needsDecrypt = true).fold(Some(_), _ => None)
      case ErrorResponse(_)      => None
    }
  }
}
