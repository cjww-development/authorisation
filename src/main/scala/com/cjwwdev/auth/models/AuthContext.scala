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

package com.cjwwdev.auth.models

import com.cjwwdev.json.JsonFormats
import org.joda.time.DateTime
import play.api.data.validation.ValidationError
import play.api.libs.json._
import play.api.libs.functional.syntax._

case class User(userId: String,
                firstName: Option[String],
                lastName: Option[String],
                orgName: Option[String],
                credentialType: String,
                role: Option[String])

object User extends JsonFormats[User] {
  val reads: Reads[User] = (
    (__ \ "userId").read[String] and
      (__ \ "firstName").readNullable[String] and
      (__ \ "lastName").readNullable[String] and
      (__ \ "orgName").readNullable[String] and
      (__ \ "credentialType").read[String] and
      (__ \ "role").readNullable[String]
    )(User.apply _).filterNot(ValidationError("Credential type was individual but either first and last name wasn't defined or org name was"))(
    user => user.credentialType == "individual" && (user.firstName.isEmpty || user.lastName.isEmpty || user.orgName.isDefined)
  ).filterNot(ValidationError("Credential type was organisation but either org name wasn't defined or first and last name was"))(
    user => user.credentialType == "organisation" && (user.orgName.isEmpty || user.firstName.isDefined || user.lastName.isDefined)
  )

  val writes: OWrites[User] = (
    (__ \ "userId").write[String] and
      (__ \ "firstName").writeNullable[String] and
      (__ \ "lastName").writeNullable[String] and
      (__ \ "orgName").writeNullable[String] and
      (__ \ "credentialType").write[String] and
      (__ \ "role").writeNullable[String]
    )(unlift(User.unapply))

  override implicit val standardFormat: OFormat[User] = OFormat(reads, writes)
}

case class AuthContext(contextId: String,
                       user: User,
                       basicDetailsUri: String,
                       enrolmentsUri: String,
                       settingsUri: String,
                       createdAt: DateTime)

object AuthContext extends JsonFormats[AuthContext] {
  override implicit val standardFormat: OFormat[AuthContext] = (
    (__ \ "contextId").format[String] and
    (__ \ "user").format[User](User.standardFormat) and
    (__ \ "basicDetailsUri").format[String] and
    (__ \ "enrolmentsUri").format[String] and
    (__ \ "settingsUri").format[String] and
    (__ \ "createdAt").format[DateTime](dateTimeRead)(dateTimeWrite)
  )(AuthContext.apply, unlift(AuthContext.unapply))
}