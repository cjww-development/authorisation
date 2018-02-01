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

package com.cjwwdev.auth.helpers

import com.cjwwdev.implicits.ImplicitHandlers
import play.api.libs.json.OWrites
import play.api.libs.ws.WSResponse

trait MockHttpResponse extends ImplicitHandlers {
  def mockWSResponse[T](statusCode: Int, bodyInput: T)(implicit writes: OWrites[T]): WSResponse = new WSResponse {
    override def cookie(name: String) = ???
    override def underlying[T]        = ???
    override def body                 = bodyInput.encryptType
    override def bodyAsBytes          = ???
    override def cookies              = ???
    override def allHeaders           = ???
    override def xml                  = ???
    override def statusText           = ???
    override def json                 = ???
    override def header(key: String)  = ???
    override def status               = statusCode
  }

  def mockWSResponseWithString(statusCode: Int, bodyInput: String): WSResponse = new WSResponse {
    override def cookie(name: String) = ???
    override def underlying[T]        = ???
    override def body                 = bodyInput.encrypt
    override def bodyAsBytes          = ???
    override def cookies              = ???
    override def allHeaders           = ???
    override def xml                  = ???
    override def statusText           = ???
    override def json                 = ???
    override def header(key: String)  = ???
    override def status               = statusCode
  }
}
