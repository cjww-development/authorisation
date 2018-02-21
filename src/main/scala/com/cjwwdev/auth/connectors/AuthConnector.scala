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
import com.cjwwdev.http.verbs.Http
import com.cjwwdev.implicits.ImplicitHandlers
import play.api.mvc.Request

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AuthConnectorImpl @Inject()(val http: Http, val configurationLoader: ConfigurationLoader) extends AuthConnector {
  val authMicroservice = configurationLoader.buildServiceUrl("auth-microservice")
  val sessionStore     = configurationLoader.buildServiceUrl("session-store")
}

trait AuthConnector extends ImplicitHandlers {
  val http: Http

  val authMicroservice: String
  val sessionStore: String

  def getCurrentUser(implicit request: Request[_]): Future[Option[CurrentUser]] = {
    http.constructHeaderPackageFromRequestHeaders.fold(Future.successful(Option.empty[CurrentUser]))(headers =>
      http.get(s"$sessionStore/session/${headers.cookieId}/context") flatMap { sessionResp =>
        val contextId = sessionResp.body.decrypt
        http.get(s"$authMicroservice/get-current-user/$contextId") map { contextResp =>
          Some(contextResp.body.decryptType[CurrentUser])
        } recover {
          case _: NotFoundException => None
        }
      }
    )
  }
}