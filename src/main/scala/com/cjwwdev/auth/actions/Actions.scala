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
import com.cjwwdev.auth.models.AuthContext
import play.api.mvc._

import scala.concurrent.Future
import scala.language.implicitConversions

trait Actions extends ActionWrappers with ActionAliases {
  val authConnector: AuthConnector

  implicit def makeFutureAction(body: AsyncPlayUserRequest): UserAction = (user: AuthContext) => Action.async(body(user))

  def authorised(unauthorisedRedirect: Call): AuthenticatedAction = new AuthenticatedBy(unauthorisedRedirect)

  class AuthenticatedBy(unauthorisedRedirect: Call) extends AuthenticatedAction with ActionAliases {
    def async(body : AsyncPlayUserRequest): Action[AnyContent] = authorised(body)

    private def authorised(body: UserAction) = {
      withAuthenticatedUser(unauthorisedRedirect) {
        implicit context => body(context)
      }
    }
  }
}
