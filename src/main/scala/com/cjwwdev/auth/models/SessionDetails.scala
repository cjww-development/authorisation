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

import java.util.UUID

import com.cjwwdev.json.JsonFormats
import play.api.libs.json._
import play.api.libs.functional.syntax._

case class SessionDetails(contextId: String, firstName: String, lastName: String) {
  def sessionMap: Map[String, String] = Map(
    "cookieId"  -> s"session${UUID.randomUUID}",
    "contextId" -> contextId,
    "firstName" -> firstName,
    "lastName"  -> lastName
  )
}

object SessionDetails extends JsonFormats[SessionDetails] {
  override implicit val standardFormat: OFormat[SessionDetails] = (
    (__ \ "contextId").format[String] and
    (__ \ "firstName").format[String] and
    (__ \ "lastName").format[String]
  )(SessionDetails.apply, unlift(SessionDetails.unapply))
}
