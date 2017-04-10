// Copyright (C) 2016-2017 the original author or authors.
// See the LICENCE.txt file distributed with this work for additional
// information regarding copyright ownership.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.cjwwdev.auth.actions

import com.cjwwdev.auth.connectors.AuthConnector
import com.cjwwdev.auth.models.AuthContext
import com.cjwwdev.logging.Logger
import play.api.mvc.{Action, AnyContent, Call, Results}

import scala.concurrent.ExecutionContext.Implicits.global

trait ActionWrappers extends Results {

  val authConnector: AuthConnector

  def withAuthenticatedUser(call : Call)(userAction: AuthContext => Action[AnyContent]): Action[AnyContent] = Action.async {
    implicit request =>
      authConnector.getContext flatMap {
        case Some(context) =>
          Logger.info(s"Authenticated as ${context.user.userId} on ${request.path}")
          userAction(context)(request)
        case _ =>
          Logger.warn(s"Unauthenticated user attempting to access ${request.path}; redirecting to login")
          Action(Redirect(call))(request)
      }
  }

  def withPotentialUser(userAction: Option[AuthContext] => Action[AnyContent]): Action[AnyContent] = Action.async {
    implicit request =>
      authConnector.getContext flatMap {
        case Some(context) =>
          Logger.info(s"Authenticated as ${context.user.userId} on ${request.path}")
          userAction(Some(context))(request)
        case _ =>
          Logger.info(s"Unauthenticated user on ${request.path}")
          userAction(None)(request)
      }
  }
}
