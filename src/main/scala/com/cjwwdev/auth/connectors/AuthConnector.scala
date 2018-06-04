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
import com.cjwwdev.config.ConfigurationLoader
import com.cjwwdev.http.exceptions.NotFoundException
import com.cjwwdev.http.responses.WsResponseHelpers
import com.cjwwdev.http.verbs.Http
import play.api.mvc.Request

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AuthConnectorImpl @Inject()(val http: Http, val configurationLoader: ConfigurationLoader) extends AuthConnector {
  val authUrl = configurationLoader.buildServiceUrl("auth-microservice")
  def authUri(cookieId: String): String = {
    configurationLoader
      .loadedConfig
      .underlying
      .getString("microservice.external-services.auth-microservice.uri")
      .replace(":sessionId", cookieId)
  }

  val sessionStoreUrl = configurationLoader.buildServiceUrl("session-store")
  def sessionStoreUri(contextId: String): String = {
    configurationLoader
      .loadedConfig
      .underlying
      .getString(s"microservice.external-services.session-store.uri")
      .replace(":contextId", contextId)
  }
}

trait AuthConnector extends WsResponseHelpers {
  val http: Http

  def authUrl: String
  def authUri(cookieId: String): String

  def sessionStoreUrl: String
  def sessionStoreUri(contextId: String): String

  def getCurrentUser(implicit request: Request[_]): Future[Option[CurrentUser]] = {
    http.constructHeaderPackageFromRequestHeaders.fold(Future(Option.empty[CurrentUser])) { headers =>
      http.get(s"$sessionStoreUrl${sessionStoreUri(headers.cookieId)}") flatMap { sessionResp =>
        val cookieId = sessionResp.toResponseString(needsDecrypt = true)
        http.get(s"$authUrl${authUri(cookieId)}") map { contextResp =>
          Some(contextResp.toDataType[CurrentUser](needsDecrypt = true))
        } recover {
          case _: NotFoundException => None
        }
      } recover {
        case _: NotFoundException => None
      }
    }
  }
}
