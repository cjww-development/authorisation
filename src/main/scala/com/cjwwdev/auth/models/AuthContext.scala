/*
 * Copyright 2017 HM Revenue & Customs
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

package com.cjwwdev.auth.models

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class AuthContext(contextId: String,
                       user: User,
                       basicDetailsUri: String,
                       enrolmentsUri: String,
                       settingsUri: String)

object AuthContext {
  implicit val format: Format[AuthContext] = (
    (__ \ "_id").format[String] and
    (__ \ "user").format[User] and
    (__ \ "basicDetailsUri").format[String] and
    (__ \ "enrolmentsUri").format[String] and
    (__ \ "settingsUri").format[String]
  )(AuthContext.apply, unlift(AuthContext.unapply))
}