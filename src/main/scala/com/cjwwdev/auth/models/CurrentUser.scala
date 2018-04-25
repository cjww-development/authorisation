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

package com.cjwwdev.auth.models

import play.api.data.validation.ValidationError
import play.api.libs.json._
import play.api.libs.functional.syntax._

case class CurrentUser(contextId: String,
                       id: String,
                       credentialType: String,
                       orgDeversityId: Option[String],
                       orgName: Option[String],
                       firstName: Option[String],
                       lastName: Option[String],
                       role: Option[String],
                       enrolments: Option[JsObject])

/*
*
* contextId
* id (userId)
* credentialType (individual / organisation)
* orgDeversityId (orgOnly)
* orgName (orgOnly)
* firstName (individualOnly)
* lastName (individualOnly)
* role (individualOnly)
* enrolments (individualOnly)
*/

object CurrentUser {
  implicit val standardFormat: OFormat[CurrentUser] = (
    (__ \ "contextId").format[String] and
    (__ \ "id").format[String] and
    (__ \ "credentialType").format[String] and
    (__ \ "orgDeversityId").formatNullable[String] and
    (__ \ "orgName").formatNullable[String] and
    (__ \ "firstName").formatNullable[String] and
    (__ \ "lastName").formatNullable[String] and
    (__ \ "role").formatNullable[String] and
    (__ \ "enrolments").formatNullable[JsObject]
  )(CurrentUser.apply, unlift(CurrentUser.unapply))
}

