import scala.sys.process._
import scala.util.Try

ThisBuild / scalaVersion      := "2.13.10"
ThisBuild / organization      := "it.pagopa"
ThisBuild / organizationName  := "Pagopa S.p.A."
Global / onChangedBuildSource := ReloadOnSourceChanges
ThisBuild / version           := ComputeVersion.version
ThisBuild / scalafmtConfig    := file(".scalafmt.conf")

lazy val noPublishSettings: SettingsDefinition =
  Seq(publish / skip := true, publish := (()), publishLocal := (()), publishTo := None)

val generateCode  = taskKey[Unit]("A task for generating the code starting from the swagger definition")
val packagePrefix = settingKey[String]("The package prefix derived from the uservice name")
val projectName   = settingKey[String]("The project name prefix derived from the uservice name")

val commonsUtils = ProjectRef(file("../commons"), "utils")

lazy val root = (project in file("."))
  .settings(name := "interop-selfcare-proxy-clients", publish / skip := true)
  .aggregate(partyProcessClient, partyManagementClient, userRegistryClient, selfcareV2Client)

cleanFiles += baseDirectory.value / "party-process-client" / "src"
cleanFiles += baseDirectory.value / "party-process-client" / "target"

lazy val partyProcessClient = project
  .in(file("party-process-client"))
  .settings(
    name                := "interop-selfcare-party-process-client",
    packagePrefix       := name.value
      .replaceFirst("interop-", "interop.")
      .replaceFirst("selfcare-", "selfcare.")
      .replaceFirst("party-process-", "partyprocess-.")
      .replaceAll("-", ""),
    projectName         := name.value
      .replaceFirst("interop-", "")
      .replaceFirst("selfcare-", ""),
    generateCode        := {
      Process(s"""openapi-generator-cli generate -t selfcare-proxy-clients/template/scala-akka-http-client
                 |                               -i selfcare-proxy-clients/party-process-client/interface-specification.yml
                 |                               -g scala-akka
                 |                               -p projectName=${projectName.value}
                 |                               -p invokerPackage=it.pagopa.${packagePrefix.value}.invoker
                 |                               -p modelPackage=it.pagopa.${packagePrefix.value}.model
                 |                               -p apiPackage=it.pagopa.${packagePrefix.value}.api
                 |                               -p modelPropertyNaming=original
                 |                               -p dateLibrary=java8
                 |                               -p entityStrictnessTimeout=15
                 |                               -o selfcare-proxy-clients/party-process-client""".stripMargin).!!
    },
    scalacOptions       := Seq(),
    libraryDependencies := Dependencies.Jars.client,
    updateOptions       := updateOptions.value.withGigahorse(false)
  )
  .dependsOn(commonsUtils)

cleanFiles += baseDirectory.value / "party-management-client" / "src"
cleanFiles += baseDirectory.value / "party-management-client" / "target"

lazy val partyManagementClient = project
  .in(file("party-management-client"))
  .settings(
    name                := "interop-selfcare-party-management-client",
    packagePrefix       := name.value
      .replaceFirst("interop-", "interop.")
      .replaceFirst("selfcare-", "selfcare.")
      .replaceFirst("party-management-", "partymanagement.")
      .replaceAll("-", ""),
    projectName         := name.value
      .replaceFirst("interop-", "")
      .replaceFirst("selfcare-", ""),
    generateCode        := {
      Process(s"""openapi-generator-cli generate -t selfcare-proxy-clients/template/scala-akka-http-client
                 |                               -i selfcare-proxy-clients/party-management-client/interface-specification.yml
                 |                               -g scala-akka
                 |                               -p projectName=${projectName.value}
                 |                               -p invokerPackage=it.pagopa.${packagePrefix.value}.invoker
                 |                               -p modelPackage=it.pagopa.${packagePrefix.value}.model
                 |                               -p apiPackage=it.pagopa.${packagePrefix.value}.api
                 |                               -p modelPropertyNaming=original
                 |                               -p dateLibrary=java8
                 |                               -o selfcare-proxy-clients/party-management-client""".stripMargin).!!
    },
    scalacOptions       := Seq(),
    libraryDependencies := Dependencies.Jars.client,
    updateOptions       := updateOptions.value.withGigahorse(false),
    noPublishSettings
  )
  .dependsOn(commonsUtils)

cleanFiles += baseDirectory.value / "user-registry-client" / "src"
cleanFiles += baseDirectory.value / "user-registry-client" / "target"

lazy val userRegistryClient = project
  .in(file("user-registry-client"))
  .settings(
    name                := "interop-selfcare-user-registry-client",
    packagePrefix       := name.value
      .replaceFirst("interop-", "interop.")
      .replaceFirst("selfcare-", "selfcare.")
      .replaceFirst("user-registry-", "userregistry.")
      .replaceAll("-", ""),
    projectName         := name.value
      .replaceFirst("interop-", "")
      .replaceFirst("selfcare-", ""),
    generateCode        := {
      Process(s"""openapi-generator-cli generate -t selfcare-proxy-clients/template/scala-akka-http-client
                 |                               -i selfcare-proxy-clients/user-registry-client/interface-specification.yml
                 |                               -g scala-akka
                 |                               -p projectName=${projectName.value}
                 |                               -p invokerPackage=it.pagopa.${packagePrefix.value}.invoker
                 |                               -p modelPackage=it.pagopa.${packagePrefix.value}.model
                 |                               -p apiPackage=it.pagopa.${packagePrefix.value}.api
                 |                               -p modelPropertyNaming=original
                 |                               -p dateLibrary=java8
                 |                               -o selfcare-proxy-clients/user-registry-client""".stripMargin).!!
    },
    scalacOptions       := Seq(),
    libraryDependencies := Dependencies.Jars.client,
    updateOptions       := updateOptions.value.withGigahorse(false),
    noPublishSettings
  )
  .dependsOn(commonsUtils)

cleanFiles += baseDirectory.value / "selfcare-v2-client" / "src"
cleanFiles += baseDirectory.value / "selfcare-v2-client" / "target"

lazy val selfcareV2Client = project
  .in(file("selfcare-v2-client"))
  .settings(
    name                := "interop-selfcare-v2-client",
    packagePrefix       := name.value
      .replaceFirst("interop-", "interop.")
      .replaceFirst("selfcare-", "selfcare.")
      .replaceFirst("v2-", "v2.")
      .replaceAll("-", ""),
    projectName         := name.value
      .replaceFirst("interop-", "")
      .replaceFirst("selfcare-", ""),
    generateCode        := {
      Process(s"""openapi-generator-cli generate -t selfcare-proxy-clients/template/scala-akka-http-client
                 |                               -i selfcare-proxy-clients/selfcare-v2-client/interface-specification.yml
                 |                               -g scala-akka
                 |                               -p projectName=${projectName.value}
                 |                               -p invokerPackage=it.pagopa.${packagePrefix.value}.invoker
                 |                               -p modelPackage=it.pagopa.${packagePrefix.value}.model
                 |                               -p apiPackage=it.pagopa.${packagePrefix.value}.api
                 |                               -p modelPropertyNaming=original
                 |                               -p dateLibrary=java8
                 |                               -o selfcare-proxy-clients/selfcare-v2-client""".stripMargin).!!
    },
    scalacOptions       := Seq(),
    libraryDependencies := Dependencies.Jars.client,
    updateOptions       := updateOptions.value.withGigahorse(false),
    noPublishSettings
  )
  .dependsOn(commonsUtils)

(Compile / compile) := ((Compile / compile) dependsOn partyProcessClient / generateCode).value
(Compile / compile) := ((Compile / compile) dependsOn partyManagementClient / generateCode).value
(Compile / compile) := ((Compile / compile) dependsOn userRegistryClient / generateCode).value
(Compile / compile) := ((Compile / compile) dependsOn selfcareV2Client / generateCode).value
