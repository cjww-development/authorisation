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
import com.cjwwdev.logging.Logging
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global

trait ActionWrappers extends Results with Logging {
  self: ActionAliases =>

  val authConnector: AuthConnector

  def withAuthenticatedUser(unauthorisedRedirect : Call)(userAction: UserAction): Action[AnyContent] = Action.async {
    implicit request =>
      authConnector.getContext flatMap {
        case Some(context) =>
          logger.info(s"Authenticated as ${context.user.id} on ${request.path}")
          userAction(context)(request)
        case _ =>
          logger.warn(s"Unauthenticated user attempting to access ${request.path}; redirecting to login")
          Action(Redirect(unauthorisedRedirect))(request)
      }
  }
}
