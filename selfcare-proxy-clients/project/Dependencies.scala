import Versions._
import sbt._

object Dependencies {

  private[this] object akka {
    lazy val namespace  = "com.typesafe.akka"
    lazy val stream     = namespace           %% "akka-stream"      % akkaVersion
    lazy val http       = namespace           %% "akka-http"        % akkaHttpVersion
    lazy val httpJson4s = "de.heikoseeberger" %% "akka-http-json4s" % akkaHttpJson4sVersions
    lazy val slf4j      = namespace           %% "akka-slf4j"       % akkaVersion
  }

  private[this] object json4s {
    lazy val namespace = "org.json4s"
    lazy val jackson   = namespace %% "json4s-jackson" % json4sVersion
    lazy val ext       = namespace %% "json4s-ext"     % json4sVersion
  }

  object Jars {

    lazy val client: Seq[ModuleID] = Seq(
      akka.stream     % Compile,
      akka.http       % Compile,
      akka.httpJson4s % Compile,
      akka.slf4j      % Compile,
      json4s.jackson  % Compile,
      json4s.ext      % Compile
    )
  }
}
