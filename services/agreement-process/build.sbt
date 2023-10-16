import ProjectSettings.ProjectFrom
import com.typesafe.sbt.packager.docker.Cmd

ThisBuild / scalaVersion      := "2.13.10"
ThisBuild / organization      := "it.pagopa"
ThisBuild / organizationName  := "Pagopa S.p.A."
Global / onChangedBuildSource := ReloadOnSourceChanges
ThisBuild / dependencyOverrides ++= Dependencies.Jars.overrides
ThisBuild / version           := ComputeVersion.version
ThisBuild / scalafmtConfig    := file(".scalafmt.conf")

lazy val noPublishSettings: SettingsDefinition =
  Seq(publish / skip := true, publish := (()), publishLocal := (()), publishTo := None)

lazy val sharedSettings: SettingsDefinition =
  Seq(scalafmtOnCompile := true) ++ noPublishSettings

lazy val generateCode = taskKey[Unit]("A task for generating the code starting from the swagger definition")

val commonsUtils       = ProjectRef(file("../../commons"), "utils")
val commonsQueue       = ProjectRef(file("../../commons"), "queue")
val commonsMailManager = ProjectRef(file("../../commons"), "mailManager")
val commonsFileManager = ProjectRef(file("../../commons"), "fileManager")
val commonsJwt         = ProjectRef(file("../../commons"), "jwtModule")
val commonsCqrs        = ProjectRef(file("../../commons"), "cqrs")

val agreementManagementClient         = ProjectRef(file("../agreement-management"), "client")
val agreementManagementModels         = ProjectRef(file("../agreement-management"), "models")
val attributeRegistryManagementModels = ProjectRef(file("../attribute-registry-management"), "models")
val authorizationManagementClient     = ProjectRef(file("../authorization-management"), "client")
val catalogManagementModels           = ProjectRef(file("../catalog-management"), "models")
val partyProcessClient                = ProjectRef(file("../../selfcare-proxy-clients"), "partyProcessClient")
val tenantManagementModels            = ProjectRef(file("../tenant-management"), "models")
val userRegistryManagementClient      = ProjectRef(file("../../selfcare-proxy-clients"), "userRegistryClient")

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

  Process(
    s"""openapi-generator-cli generate -t services/${projectName.value}/template/scala-akka-http-server
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
    s"""openapi-generator-cli generate -t services/${projectName.value}/template/scala-akka-http-client
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

(Compile / compile) := ((Compile / compile) dependsOn generateCode).value
(Test / test)       := ((Test / test) dependsOn generateCode).value

cleanFiles += baseDirectory.value / "generated" / "src"
cleanFiles += baseDirectory.value / "generated" / "target"

cleanFiles += baseDirectory.value / "client" / "src"
cleanFiles += baseDirectory.value / "client" / "target"

cleanFiles += baseDirectory.value / "lifecycle" / "target"
cleanFiles += baseDirectory.value / "events" / "target"

val runStandalone = inputKey[Unit]("Run the app using standalone configuration")
runStandalone := {
  task(
    System.setProperty("config.file", "services/${projectName.value}/src/main/resources/application-standalone.conf")
  ).value
  (Compile / run).evaluated
}

lazy val generated = project
  .in(file("generated"))
  .settings(scalacOptions := Seq(), libraryDependencies := Dependencies.Jars.`server`, sharedSettings)
  .dependsOn(commonsUtils)
  .setupBuildInfo

lazy val client = project
  .in(file("client"))
  .settings(
    name                := "interop-be-agreement-process-client",
    scalacOptions       := Seq(),
    libraryDependencies := Dependencies.Jars.client,
    updateOptions       := updateOptions.value.withGigahorse(false),
    sharedSettings
  )
  .dependsOn(commonsUtils)

lazy val events = project
  .in(file("events"))
  .settings(
    name                := "interop-be-agreement-process-events",
    libraryDependencies := Dependencies.Jars.events,
    sharedSettings,
    Docker / publish    := {}
  )
  .dependsOn(commonsUtils, commonsQueue)

lazy val lifecycle = project
  .in(file("lifecycle"))
  .settings(
    name                := "interop-be-agreement-process-lifecycle",
    libraryDependencies := Dependencies.Jars.lifecycle,
    sharedSettings,
    Docker / publish    := {}
  )
  .dependsOn(commonsUtils, commonsQueue, tenantManagementModels, catalogManagementModels, agreementManagementModels)

lazy val root = (project in file("."))
  .settings(
    name                        := "interop-be-agreement-process",
    Test / parallelExecution    := false,
    Test / fork                 := true,
    Test / javaOptions += s"-Dconfig.file=services/${projectName.value}/src/test/resources/application-test.conf",
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
  .aggregate(client, lifecycle, events)
  .dependsOn(
    generated,
    lifecycle,
    events,
    agreementManagementClient,
    agreementManagementModels,
    attributeRegistryManagementModels,
    authorizationManagementClient,
    catalogManagementModels,
    tenantManagementModels,
    commonsMailManager,
    commonsUtils,
    commonsFileManager,
    commonsJwt,
    commonsCqrs,
    commonsQueue,
    partyProcessClient,
    userRegistryManagementClient
  )
  .enableContractTest
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(DockerPlugin)
  .enablePlugins(NoPublishPlugin)
  .setupBuildInfo
