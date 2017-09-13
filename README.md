[![Apache-2.0 license](http://img.shields.io/badge/license-Apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
[ ![Download](https://api.bintray.com/packages/cjww-development/releases/authorisation/images/download.svg) ](https://bintray.com/cjww-development/releases/authorisation/_latestVersion)

authorisation
=================

Mechanisms to determine if a user is authenticated and subsequently authorised to see a page

To utilise this library add this to your sbt build file

```
"com.cjww-dev.libs" % "authorisation_2.11" % "1.12.0" 
```

## About
#### Actions.scala
Extend a class with Actions and utilise authorisedFor() to secure an action.

If the user is not authorised they will be directed to the specified URL provided in authorisedFor()

```scala
    class ExampleController @Inject()(val authConnector: AuthConnector) extends Controller with Actions {
      def exampleAction: Action[AnyContent] = authorisedFor("/login/page").async {
        implicit user =>
          implicit request =>
            Future.successful(Ok) 
      }
    }
```

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")
