import ProjectSettings._
import com.typesafe.sbt.packager.docker.Cmd

ThisBuild / scalaVersion      := projectScalaVersion
ThisBuild / organization      := "it.pagopa"
ThisBuild / organizationName  := "Pagopa S.p.A."
Global / onChangedBuildSource := ReloadOnSourceChanges
ThisBuild / dependencyOverrides ++= Dependencies.Jars.overrides
ThisBuild / version           := ComputeVersion.version
ThisBuild / githubOwner       := githubRepoOwner
ThisBuild / githubRepository  := githubRepositoryName
ThisBuild / resolvers += Resolver.githubPackages(githubRepoOwner)
ThisBuild / scalafmtConfig    := file(".scalafmt.conf")

inThisBuild(sbtGithubActionsSettings)

lazy val generateCode = taskKey[Unit]("A task for generating the code starting from the swagger definition")

val commonsUtils       = ProjectRef(file("../../commons"), "utils")
val commonsQueue       = ProjectRef(file("../../commons"), "queue")
val commonsFileManager = ProjectRef(file("../../commons"), "fileManager")
val commonsJwt         = ProjectRef(file("../../commons"), "jwtModule")
val commonsCqrs        = ProjectRef(file("../../commons"), "cqrs")
val commonsParser      = ProjectRef(file("../../commons"), "parser")

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

val runStandalone = inputKey[Unit]("Run the app using standalone configuration")
runStandalone := {
  task(
    System.setProperty("config.file", s"services/${projectName.value}/src/main/resources/application-standalone.conf")
  ).value
  (Compile / run).evaluated
}

(Compile / compile) := ((Compile / compile) dependsOn generateCode).value
(Test / test)       := ((Test / test) dependsOn generateCode).value

Compile / PB.targets := Seq(scalapb.gen() -> (Compile / sourceManaged).value / "protobuf")

cleanFiles += baseDirectory.value / "generated" / "src"

cleanFiles += baseDirectory.value / "generated" / "target"

cleanFiles += baseDirectory.value / "client" / "src"

cleanFiles += baseDirectory.value / "client" / "target"

lazy val generated = project
  .configs(IntegrationTest)
  .settings(Defaults.itSettings: _*)
  .in(file("generated"))
  .settings(
    scalacOptions       := Seq(),
    scalafmtOnCompile   := true,
    libraryDependencies := Dependencies.Jars.`server`,
    publish / skip      := true,
    publish             := (()),
    publishLocal        := (()),
    publishTo           := None
  )
  .dependsOn(commonsUtils)
  .setupBuildInfo

lazy val models = project
  .in(file("models"))
  .settings(
    name                := "interop-be-catalog-management-models",
    libraryDependencies := Dependencies.Jars.models,
    scalafmtOnCompile   := true,
    Docker / publish    := {}
  )
  .dependsOn(commonsUtils, commonsQueue)

lazy val client = project
  .in(file("client"))
  .settings(
    name                := "interop-be-catalog-management-client",
    scalacOptions       := Seq(),
    scalafmtOnCompile   := true,
    libraryDependencies := Dependencies.Jars.client,
    updateOptions       := updateOptions.value.withGigahorse(false),
    Docker / publish    := {}
  )
  .dependsOn(commonsUtils)

lazy val root = (project in file("."))
  .configs(IntegrationTest)
  .settings(Defaults.itSettings: _*)
  .settings(
    name                        := "interop-be-catalog-management",
    Test / parallelExecution    := false,
    Test / fork                 := true,
    Test / javaOptions += "-Dconfig.file=src/test/resources/application-test.conf",
    IntegrationTest / fork      := true,
    IntegrationTest / javaOptions += "-Dconfig.file=src/it/resources/application-it.conf",
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
  .aggregate(client, models)
  .dependsOn(generated, models, commonsUtils, commonsQueue, commonsFileManager, commonsJwt, commonsCqrs, commonsParser)
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(DockerPlugin)
  .enablePlugins(NoPublishPlugin)
  .setupBuildInfo
