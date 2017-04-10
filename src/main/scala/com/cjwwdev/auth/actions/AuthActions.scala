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

trait Actions extends AuthActions with ActionWrappers {

  self: AuthConnector =>

  private type PlayRequest = (Request[AnyContent] => Result)
  private type AsyncPlayRequest = (Request[AnyContent] => Future[Result])
  private type PlayUserRequest = AuthContext => PlayRequest
  private type AsyncPlayUserRequest = AuthContext => AsyncPlayRequest

  private type OptionPlayUserRequest = Option[AuthContext] => PlayRequest
  private type OptionAsyncPlayUserRequest = Option[AuthContext] => AsyncPlayRequest

  type UserAction = AuthContext => Action[AnyContent]
  type OptionUserAction = Option[AuthContext] => Action[AnyContent]

  implicit def makeAction(body: PlayUserRequest): UserAction = (user: AuthContext) => Action(body(user))
  implicit def makeFutureAction(body: AsyncPlayUserRequest): UserAction = (user: AuthContext) => Action.async(body(user))

  implicit def makeOptionAction(body : OptionPlayUserRequest) : OptionUserAction = (user : Option[AuthContext]) => Action(body(user))
  implicit def makeOptionFutureAction(body : OptionAsyncPlayUserRequest) : OptionUserAction =
    (userAccount : Option[AuthContext]) => Action.async(body(userAccount))

  def authorisedFor(loginCall: Call): AuthenticatedAction = new AuthenticatedBy(loginCall)
  def unauthenticatedAction: UnauthenticatedAction = new Unauthenticated()

  class AuthenticatedBy(loginCall: Call) extends AuthenticatedAction {
    def async(body : AsyncPlayUserRequest) : Action[AnyContent] = authorised(body)

    private def authorised(body : UserAction) = {
      withAuthenticatedUser(loginCall) {
        implicit account => body(account)
      }
    }
  }

  class Unauthenticated extends UnauthenticatedAction {
    def apply(body : OptionPlayUserRequest) : Action[AnyContent] = unauthorised(body)
    def async(body : OptionAsyncPlayUserRequest) : Action[AnyContent] = unauthorised(body)

    private def unauthorised(body : OptionUserAction) = {
      withPotentialUser {
        implicit account => body(account)
      }
    }
  }
}

trait AuthActions {
  def authorisedFor(loginCall: Call): AuthenticatedAction
  def unauthenticatedAction: UnauthenticatedAction
}
