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
package com.cjwwdev.auth.connectors

import javax.inject.{Inject, Singleton}

import com.cjwwdev.auth.config.ApplicationConfiguration
import com.cjwwdev.auth.models.AuthContext
import com.cjwwdev.http.utils.SessionUtils
import com.cjwwdev.http.verbs.Http
import com.cjwwdev.security.encryption.DataSecurity
import play.api.mvc.Request
import play.api.http.Status.{OK, NOT_FOUND}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class AuthConnector @Inject()(http: Http) extends ApplicationConfiguration with SessionUtils {
  def getContext(implicit request: Request[_]): Future[Option[AuthContext]] = {
    http.GET(s"$authMicroservice/get-context/$getContextId") map { resp =>
      resp.status match {
        case OK => DataSecurity.decryptIntoType[AuthContext](resp.body)
        case NOT_FOUND => None
      }
    }
  }
}
