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
val commonsFileManager = ProjectRef(file("../../commons"), "fileManager")
val commonsJwt         = ProjectRef(file("../../commons"), "jwtModule")
val commonsRateLimiter = ProjectRef(file("../../commons"), "rateLimiter")
val commonsSigner      = ProjectRef(file("../../commons"), "signer")
val commonsParser      = ProjectRef(file("../../commons"), "parser")

val agreementProcessClient         = ProjectRef(file("../agreement-process"), "client")
val agreementProcessLifecycle      = ProjectRef(file("../agreement-process"), "lifecycle")
val attributeRegistryProcessClient = ProjectRef(file("../attribute-registry-process"), "client")
val authorizationProcessClient     = ProjectRef(file("../authorization-process"), "client")
val clientAssertionValidation      = ProjectRef(file("../authorization-server"), "clientAssertionValidation")
val catalogManagementModels        = ProjectRef(file("../catalog-management"), "models")
val catalogProcessClient           = ProjectRef(file("../catalog-process"), "client")
val partyManagementClient          = ProjectRef(file("../../selfcare-proxy-clients"), "partyManagementClient")
val partyProcessClient             = ProjectRef(file("../../selfcare-proxy-clients"), "partyProcessClient")
val purposeProcessClient           = ProjectRef(file("../purpose-process"), "client")
val tenantManagementModels         = ProjectRef(file("../tenant-management"), "models")
val tenantProcessClient            = ProjectRef(file("../tenant-process"), "client")
val userRegistryManagementClient   = ProjectRef(file("../../selfcare-proxy-clients"), "userRegistryClient")
val selfcareV2Client               = ProjectRef(file("../../selfcare-proxy-clients"), "selfcareV2Client")

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
       |                               -p modelPropertyNaming=original
       |                               -p dateLibrary=java8
       |                               -p entityStrictnessTimeout=15
       |                               -o services/${projectName.value}/generated""".stripMargin
  ).!!

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

lazy val root = (project in file("."))
  .settings(
    name                        := "interop-be-backend-for-frontend",
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
  .dependsOn(
    generated,
    commonsUtils,
    commonsJwt,
    commonsRateLimiter,
    commonsSigner,
    commonsFileManager,
    commonsParser,
    clientAssertionValidation,
    authorizationProcessClient,
    agreementProcessClient,
    agreementProcessLifecycle,
    catalogProcessClient,
    catalogManagementModels,
    partyProcessClient,
    partyManagementClient,
    tenantManagementModels,
    tenantProcessClient,
    purposeProcessClient,
    userRegistryManagementClient,
    attributeRegistryProcessClient,
    selfcareV2Client
  )
  .enableContractTest
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(DockerPlugin)
  .enablePlugins(NoPublishPlugin)
  .setupBuildInfo

Test / fork := true
Test / javaOptions += s"-Dconfig.file=services/${projectName.value}/src/test/resources/application-test.conf"
