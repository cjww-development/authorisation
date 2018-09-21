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

import com.cjwwdev.auth.models.CurrentUser
import com.cjwwdev.http.headers.HttpHeaders
import com.cjwwdev.logging.Logging
import com.cjwwdev.responses.ApiResponse
import com.cjwwdev.implicits.ImplicitDataSecurity._
import com.cjwwdev.security.deobfuscation.DeObfuscation._
import com.cjwwdev.security.deobfuscation.DeObfuscator
import com.typesafe.config.ConfigFactory
import play.api.mvc.Results.Forbidden
import play.api.http.Status.FORBIDDEN
import play.api.mvc.{Request, Result}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

sealed trait AuthorisationResult
case class Authorised(currentUser: CurrentUser) extends AuthorisationResult
case object Authenticated extends AuthorisationResult
case object NotAuthorised extends AuthorisationResult

trait BaseAuth extends HttpHeaders with Logging with ApiResponse {
  private val configuration = ConfigFactory.load

  val idSet: List[String] = configuration.getString("microservice.allowedApps").decrypt[String].fold(
    _.split(",").toList,
    err => throw err
  )

  protected def applicationVerification(f: => Future[Result])(implicit request: Request[_]): Future[Result] = {
    validateAppId match {
      case Authenticated  => f
      case _ => withFutureJsonResponseBody(FORBIDDEN, "The calling application could not be verified") { json =>
        Future(Forbidden(json))
      }
    }
  }

  protected def validateAppId(implicit request: Request[_]): AuthorisationResult = {
    constructHeaderPackageFromRequestHeaders.fold(notAuthorised("AppID not found in the header package"))( headerPackage =>
      if(idSet.contains(headerPackage.appId)) Authenticated else notAuthorised("API CALL FROM UNKNOWN SOURCE - ACTION DENIED")
    )
  }

  private def notAuthorised(msg: String): AuthorisationResult = {
    logger.error(s"[checkAuth] - $msg")
    NotAuthorised
  }
}
