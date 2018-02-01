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

package com.cjwwdev.auth.models

import java.util.UUID

import com.cjwwdev.auth.helpers.JsonValidation
import org.scalatestplus.play.PlaySpec
import play.api.data.validation.ValidationError
import play.api.libs.json.{JsPath, JsSuccess, Json}

class UserSpec extends PlaySpec with JsonValidation {

  val uuid = UUID.randomUUID()

  "User" should {
    "successfully read into json" when {
      "the credential type is individual" in {
        val expectedModel = User(
          id             = s"user-$uuid",
          firstName      = Some("testFirstName"),
          lastName       = Some("testLastName"),
          orgName        = None,
          credentialType = "individual",
          role           = None
        )

        val inputJson = Json.parse(
          s"""
            |{
            | "id" : "user-$uuid",
            | "firstName" : "testFirstName",
            | "lastName" : "testLastName",
            | "credentialType" : "individual"
            |}
          """.stripMargin
        )

        Json.fromJson[User](inputJson) mustBe JsSuccess(expectedModel)
      }

      "the credential type is organisation" in {
        val expectedModel = User(
          id             = s"user-$uuid",
          firstName      = None,
          lastName       = None,
          orgName        = Some("testOrgName"),
          credentialType = "organisation",
          role           = None
        )

        val inputJson = Json.parse(
          s"""
             |{
             | "id" : "user-$uuid",
             | "orgName" : "testOrgName",
             | "credentialType" : "organisation"
             |}
          """.stripMargin
        )

        Json.fromJson[User](inputJson) mustBe JsSuccess(expectedModel)
      }
    }

    "fail reads" when {
      "credential type is individual but first and last name aren't defined" in {
        val inputJson = Json.parse(
          s"""
             |{
             | "id" : "user-$uuid",
             | "credentialType" : "individual"
             |}
          """.stripMargin
        )

        val result = Json.fromJson[User](inputJson)

        val expectedErrors = Map(
          JsPath() -> Seq(ValidationError("Credential type was individual but either first and last name wasn't defined or org name was"))
        )

        shouldHaveErrors(result, expectedErrors)
      }

      "credential type is individual but first and last name aren't defined but org name is" in {
        val inputJson = Json.parse(
          s"""
             |{
             | "id" : "user-$uuid",
             | "orgName" : "testOrgName",
             | "credentialType" : "individual"
             |}
          """.stripMargin
        )

        val result = Json.fromJson[User](inputJson)

        val expectedErrors = Map(
          JsPath() -> Seq(ValidationError("Credential type was individual but either first and last name wasn't defined or org name was"))
        )

        shouldHaveErrors(result, expectedErrors)
      }

      "credential type is organisation but first and last name are defined but org name isn't" in {
        val inputJson = Json.parse(
          s"""
             |{
             | "id" : "user-$uuid",
             | "firstName" : "testFirstName",
             | "lastName" : "testLastName",
             | "credentialType" : "organisation"
             |}
          """.stripMargin
        )

        val result = Json.fromJson[User](inputJson)

        val expectedErrors = Map(
          JsPath() -> Seq(ValidationError("Credential type was organisation but either org name wasn't defined or first and last name was"))
        )

        shouldHaveErrors(result, expectedErrors)
      }

      "credential type is organisation but org name isn't defined" in {
        val inputJson = Json.parse(
          s"""
             |{
             | "id" : "user-$uuid",
             | "credentialType" : "organisation"
             |}
          """.stripMargin
        )

        val result = Json.fromJson[User](inputJson)

        val expectedErrors = Map(
          JsPath() -> Seq(ValidationError("Credential type was organisation but either org name wasn't defined or first and last name was"))
        )

        shouldHaveErrors(result, expectedErrors)
      }
    }
  }
}
