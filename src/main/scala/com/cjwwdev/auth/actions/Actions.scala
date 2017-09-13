// Copyright (C) 2011-2012 the original author or authors.
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
import play.api.mvc._

import scala.concurrent.Future
import scala.language.implicitConversions

trait Actions extends ActionWrappers {

  val authConnector: AuthConnector

  private type AsyncPlayRequest     = Request[AnyContent] => Future[Result]
  private type AsyncPlayUserRequest = AuthContext => AsyncPlayRequest

  type UserAction                   = AuthContext => Action[AnyContent]

  implicit def makeFutureAction(body: AsyncPlayUserRequest): UserAction = (user: AuthContext) => Action.async(body(user))

  def authorisedFor(loginCall: Call): AuthenticatedAction = new AuthenticatedBy(loginCall)

  class AuthenticatedBy(loginCall: Call) extends AuthenticatedAction {
    def async(body : AsyncPlayUserRequest): Action[AnyContent] = authorised(body)

    private def authorised(body: UserAction) = {
      withAuthenticatedUser(loginCall) {
        implicit account => body(account)
      }
    }
  }
}
