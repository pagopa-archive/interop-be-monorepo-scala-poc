import ProjectSettings.ProjectFrom
import com.typesafe.sbt.packager.docker.Cmd

ThisBuild / scalaVersion      := "2.13.10"
ThisBuild / organization      := "it.pagopa"
ThisBuild / organizationName  := "Pagopa S.p.A."
Global / onChangedBuildSource := ReloadOnSourceChanges
ThisBuild / version           := ComputeVersion.version
ThisBuild / scalafmtConfig    := file(".scalafmt.conf")

lazy val noPublishSettings: SettingsDefinition =
  Seq(publish / skip := true, publish := (()), publishLocal := (()), publishTo := None)

lazy val sharedSettings: SettingsDefinition =
  Seq(scalafmtOnCompile := true) ++ noPublishSettings

lazy val generateCode = taskKey[Unit]("A task for generating the code starting from the swagger definition")

val commonsUtils = ProjectRef(file("../../commons"), "utils")
val commonsJwt   = ProjectRef(file("../../commons"), "jwtModule")

val packagePrefix = settingKey[String]("The package prefix derived from the uservice name")

packagePrefix := name.value
  .replaceFirst("interop-", "interop.")
  .replaceFirst("be-", "")
  .replaceAll("-", "")

val projectName = settingKey[String]("The project name prefix derived from the uservice name")

projectName := name.value
  .replaceFirst("interop-", "")
  .replaceFirst("be-", "")

generateCode := {
  import sys.process._

  val openApiCommand: String = {
    if (System.getProperty("os.name").toLowerCase.contains("win")) {
      "openapi-generator-cli-win.bat"
    } else {
      "openapi-generator-cli"
    }
  }

  Process(
    s"""${openApiCommand} generate -t services/${projectName.value}/template/scala-akka-http-server
       |                               -i services/${projectName.value}/src/main/resources/interface-specification.yml
       |                               -g scala-akka-http-server
       |                               -p projectName=${projectName.value}
       |                               -p invokerPackage=it.pagopa.${packagePrefix.value}.server
       |                               -p modelPackage=it.pagopa.${packagePrefix.value}.model
       |                               -p apiPackage=it.pagopa.${packagePrefix.value}.api
       |                               -p dateLibrary=java8
       |                               -p entityStrictnessTimeout=15
       |                               -o services/${projectName.value}/generated""".stripMargin
  ).!!

  Process(
    s"""${openApiCommand} generate -t services/${projectName.value}/template/scala-akka-http-client
       |                               -i services/${projectName.value}/src/main/resources/interface-specification.yml
       |                               -g scala-akka
       |                               -p projectName=${projectName.value}
       |                               -p invokerPackage=it.pagopa.${packagePrefix.value}.client.invoker
       |                               -p modelPackage=it.pagopa.${packagePrefix.value}.client.model
       |                               -p apiPackage=it.pagopa.${packagePrefix.value}.client.api
       |                               -p dateLibrary=java8
       |                               -o services/${projectName.value}/client""".stripMargin
  ).!!

}

val runStandalone = inputKey[Unit]("Run the app using standalone configuration")

runStandalone := {
  task(
    System.setProperty("config.file", s"services/${projectName.value}/src/main/resources/application-standalone.conf")
  ).value
  (Compile / run).evaluated
}

(Compile / compile) := ((Compile / compile) dependsOn generateCode).value
(Test / test)       := ((Test / test) dependsOn generateCode).value

cleanFiles += baseDirectory.value / "generated" / "src"

cleanFiles += baseDirectory.value / "generated" / "target"

cleanFiles += baseDirectory.value / "client" / "src"

cleanFiles += baseDirectory.value / "client" / "target"

lazy val generated = project
  .in(file("generated"))
  .settings(scalacOptions := Seq(), libraryDependencies := Dependencies.Jars.`server`, sharedSettings)
  .dependsOn(commonsUtils)
  .setupBuildInfo

lazy val client = project
  .in(file("client"))
  .settings(
    name                := "interop-be-party-registry-proxy-client",
    scalacOptions       := Seq(),
    libraryDependencies := Dependencies.Jars.client,
    updateOptions       := updateOptions.value.withGigahorse(false),
    sharedSettings
  )
  .dependsOn(commonsUtils)

lazy val root = (project in file("."))
  .settings(
    name                        := "interop-be-party-registry-proxy",
    Test / parallelExecution    := false,
    scalafmtOnCompile           := true,
    dockerBuildOptions ++= Seq("--network=host"),
    dockerRepository            := Some(System.getenv("ECR_REGISTRY")),
    dockerBaseImage             := "adoptopenjdk:11-jdk-hotspot",
    daemonUser                  := "daemon",
    Docker / version            := (ThisBuild / version).value.replaceAll("-SNAPSHOT", "-latest").toLowerCase,
    Docker / packageName        := s"${name.value}",
    Docker / dockerExposedPorts := Seq(8080),
    Docker / maintainer         := "https://pagopa.it",
    libraryDependencies         := Dependencies.Jars.`server`,
    dockerCommands += Cmd("LABEL", s"org.opencontainers.image.source https://github.com/pagopa/${name.value}")
  )
  .aggregate(client)
  .dependsOn(generated, commonsUtils, commonsJwt)
  .enablePlugins(JavaAppPackaging, JavaAgent, DockerPlugin)
  .enablePlugins(NoPublishPlugin)
  .setupBuildInfo

Test / fork := true
Test / javaOptions += s"-Dconfig.file=services/${projectName.value}/src/test/resources/application-test.conf"
