import Versions._
import sbt._

object Dependencies {

  private[this] object akka {
    lazy val namespace           = "com.typesafe.akka"
    lazy val actorTyped          = namespace                       %% "akka-actor-typed"       % akkaVersion
    lazy val actor               = namespace                       %% "akka-actor"             % akkaVersion
    lazy val persistence         = namespace                       %% "akka-persistence-typed" % akkaVersion
    lazy val stream              = namespace                       %% "akka-stream"            % akkaVersion
    lazy val http                = namespace                       %% "akka-http"              % akkaHttpVersion
    lazy val httpJson            = namespace                       %% "akka-http-spray-json"   % akkaHttpVersion
    lazy val httpJson4s          = "de.heikoseeberger"             %% "akka-http-json4s"       % "1.38.2"
    lazy val management          = "com.lightbend.akka.management" %% "akka-management"        % akkaManagementVersion
    lazy val managementLogLevels =
      "com.lightbend.akka.management" %% "akka-management-loglevels-logback" % akkaManagementVersion
    lazy val slf4j          = namespace %% "akka-slf4j"               % akkaVersion
    lazy val testkit        = namespace %% "akka-actor-testkit-typed" % akkaVersion
    lazy val httpTestkit    = namespace %% "akka-http-testkit"        % akkaHttpVersion
    lazy val untypedTestkit = namespace %% "akka-testkit"             % akkaVersion
  }

  private[this] object opensaml {
    lazy val namespace = "org.opensaml"
    lazy val core      = namespace % "opensaml" % samlVersion
  }

  private[this] object cats {
    lazy val namespace = "org.typelevel"
    lazy val core      = namespace %% "cats-core" % catsVersion
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

  private[this] object mustache {
    lazy val mustache = "com.github.spullara.mustache.java" % "compiler" % mustacheVersion
  }

  private[this] object scalatest {
    lazy val namespace = "org.scalatest"
    lazy val core      = namespace %% "scalatest" % scalatestVersion
  }

  private[this] object scalamock {
    lazy val namespace = "org.scalamock"
    lazy val core      = namespace %% "scalamock" % scalaMockVersion
  }
  private[this] object tika      {
    lazy val namespace = "org.apache.tika"
    lazy val core      = namespace % "tika-core" % tikaVersion
  }

  private[this] object scanamo {
    lazy val scanamo = "org.scanamo" %% "scanamo"         % scanamoVersion
    lazy val testkit = "org.scanamo" %% "scanamo-testkit" % scanamoVersion
  }

  object Jars {
    lazy val overrides: Seq[ModuleID] =
      Seq(jackson.annotations % Compile, jackson.core % Compile, jackson.databind % Compile)
    lazy val `server`: Seq[ModuleID]  = Seq(
      // For making Java 12 happy
      "javax.annotation"       % "javax.annotation-api" % "1.3.2" % "compile",
      //
      akka.actor               % Compile,
      akka.actorTyped          % Compile,
      akka.http                % Compile,
      akka.httpJson            % Compile,
      akka.management          % Compile,
      akka.managementLogLevels % Compile,
      akka.persistence         % Compile,
      akka.slf4j               % Compile,
      akka.stream              % Compile,
      cats.core                % Compile,
      logback.classic          % Compile,
      mustache.mustache        % Compile,
      tika.core                % Compile,
      scanamo.scanamo          % Compile,
      opensaml.core            % Compile,
      akka.httpTestkit         % Test,
      akka.testkit             % Test,
      akka.untypedTestkit      % Test,
      scalamock.core           % Test,
      scalatest.core           % Test
    )

  }
}
