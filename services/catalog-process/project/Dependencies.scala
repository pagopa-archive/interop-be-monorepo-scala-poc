import Versions._
import sbt._

object Dependencies {

  private[this] object akka {
    lazy val namespace           = "com.typesafe.akka"
    lazy val actorTyped          = namespace                       %% "akka-actor-typed"     % akkaVersion
    lazy val stream              = namespace                       %% "akka-stream"          % akkaVersion
    lazy val http                = namespace                       %% "akka-http"            % akkaHttpVersion
    lazy val httpJson            = namespace                       %% "akka-http-spray-json" % akkaHttpVersion
    lazy val httpJson4s          = "de.heikoseeberger"             %% "akka-http-json4s"     % akkaHttpJson4sVersion
    lazy val management          = "com.lightbend.akka.management" %% "akka-management"      % akkaManagementVersion
    lazy val managementLogLevels =
      "com.lightbend.akka.management" %% "akka-management-loglevels-logback" % akkaManagementVersion
    lazy val slf4j       = namespace %% "akka-slf4j"               % akkaVersion
    lazy val testkit     = namespace %% "akka-actor-testkit-typed" % akkaVersion
    lazy val httpTestkit = namespace %% "akka-http-testkit"        % akkaHttpVersion
  }

  private[this] object json4s {
    lazy val namespace = "org.json4s"
    lazy val jackson   = namespace %% "json4s-jackson" % json4sVersion
    lazy val ext       = namespace %% "json4s-ext"     % json4sVersion
  }

  private[this] object jackson {
    lazy val namespace   = "com.fasterxml.jackson.core"
    lazy val core        = namespace % "jackson-core"        % jacksonVersion
    lazy val annotations = namespace % "jackson-annotations" % jacksonVersion
    lazy val databind    = namespace % "jackson-databind"    % jacksonVersion
  }

  private[this] object logback {
    lazy val namespace = "ch.qos.logback"
    lazy val classic   = namespace % "logback-classic" % logbackVersion
  }

  private[this] object cats {
    lazy val namespace = "org.typelevel"
    lazy val core      = namespace %% "cats-core" % catsVersion
  }

  private[this] object commons {
    lazy val fileUpload = "commons-fileupload" % "commons-fileupload" % commonsFileUploadVersion
  }

  private[this] object mustache {
    lazy val namespace = "com.github.spullara.mustache.java"
    lazy val compiler  = namespace % "compiler" % mustacheVersion
  }

  private[this] object resilience4j {
    lazy val namespace   = "io.github.resilience4j"
    lazy val rateLimiter = namespace % "resilience4j-ratelimiter" % resilience4jVersion
  }

  private[this] object scalatest {
    lazy val namespace = "org.scalatest"
    lazy val core      = namespace %% "scalatest" % scalatestVersion
  }

  private[this] object scalamock {
    lazy val namespace = "org.scalamock"
    lazy val core      = namespace %% "scalamock" % scalaMockVersion
  }

  object Jars {
    lazy val overrides: Seq[ModuleID] =
      Seq(jackson.annotations % Compile, jackson.core % Compile, jackson.databind % Compile)
    lazy val `server`: Seq[ModuleID]  = Seq(
      // For making Java 12 happy
      "javax.annotation"                   % "javax.annotation-api" % "1.3.2" % "compile",
      //
      akka.actorTyped                      % Compile,
      akka.management                      % Compile,
      akka.managementLogLevels             % Compile,
      akka.stream                          % Compile,
      akka.http                            % Compile,
      akka.httpJson                        % Compile,
      cats.core                            % Compile,
      commons.fileUpload                   % Compile,
      mustache.compiler                    % Compile,
      logback.classic                      % Compile,
      akka.slf4j                           % Compile,
      resilience4j.rateLimiter             % Compile,
      akka.testkit                         % Test,
      akka.httpTestkit                     % Test,
      scalatest.core                       % Test,
      scalamock.core                       % Test
    )
    lazy val client: Seq[ModuleID]    =
      Seq(akka.stream, akka.http, akka.httpJson4s, akka.slf4j, json4s.jackson, json4s.ext).map(_ % Compile)
  }
}
