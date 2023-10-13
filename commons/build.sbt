import ProjectSettings.ProjectFrom

ThisBuild / scalaVersion      := "2.13.10"
ThisBuild / organization      := "it.pagopa"
ThisBuild / organizationName  := "Pagopa S.p.A."
ThisBuild / version           := ComputeVersion.version
Global / onChangedBuildSource := ReloadOnSourceChanges
ThisBuild / scalafmtConfig    := file(".scalafmt.conf")

val fileManagerModuleName  = "file-manager"
val mailManagerModuleName  = "mail-manager"
val jwtModuleName          = "jwt"
val signerModuleName       = "signer"
val utilsModuleName        = "utils"
val queueModuleName        = "queue-manager"
val cqrsModuleName         = "cqrs"
val rateLimiterModuleName  = "rate-limiter"
val parserModuleName       = "parser"
val riskAnalysisModuleName = "risk-analysis"

cleanFiles += baseDirectory.value / cqrsModuleName / "target"
cleanFiles += baseDirectory.value / fileManagerModuleName / "target"
cleanFiles += baseDirectory.value / mailManagerModuleName / "target"
cleanFiles += baseDirectory.value / jwtModuleName / "target"
cleanFiles += baseDirectory.value / rateLimiterModuleName / "target"
cleanFiles += baseDirectory.value / signerModuleName / "target"
cleanFiles += baseDirectory.value / utilsModuleName / "target"
cleanFiles += baseDirectory.value / queueModuleName / "target"
cleanFiles += baseDirectory.value / parserModuleName / "target"
cleanFiles += baseDirectory.value / riskAnalysisModuleName / "target"

lazy val noPublishSettings: SettingsDefinition =
  Seq(publish / skip := true, publish := (()), publishLocal := (()), publishTo := None)

lazy val sharedSettings: SettingsDefinition =
  Seq(scalafmtOnCompile := true, libraryDependencies ++= Dependencies.Jars.commonDependencies) ++ noPublishSettings

lazy val utils = project
  .in(file(utilsModuleName))
  .settings(
    name                     := "interop-commons-utils",
    libraryDependencies ++= Dependencies.Jars.utilsDependencies,
    Test / parallelExecution := false,
    sharedSettings
  )
  .setupBuildInfo

lazy val fileManager = project
  .in(file(fileManagerModuleName))
  .settings(
    name := "interop-commons-file-manager",
    sharedSettings,
    libraryDependencies ++= Dependencies.Jars.fileDependencies
  )
  .dependsOn(utils)
  .setupBuildInfo

lazy val mailManager = project
  .in(file(mailManagerModuleName))
  .settings(
    name        := "interop-commons-mail-manager",
    libraryDependencies ++= Dependencies.Jars.mailDependencies,
    Test / fork := true,
    sharedSettings
  )
  .dependsOn(utils)
  .setupBuildInfo

lazy val signer = project
  .in(file(signerModuleName))
  .settings(
    name        := "interop-commons-signer",
    libraryDependencies ++= Dependencies.Jars.signerDependencies,
    Test / fork := true,
    sharedSettings
  )
  .dependsOn(utils)
  .setupBuildInfo

lazy val jwtModule = project
  .in(file(jwtModuleName))
  .settings(
    name        := "interop-commons-jwt",
    libraryDependencies ++= Dependencies.Jars.jwtDependencies,
    Test / fork := true,
    Test / javaOptions += "-Dconfig.file=src/test/resources/reference.conf",
    sharedSettings
  )
  .dependsOn(utils, signer)
  .setupBuildInfo

lazy val queue = project
  .in(file(queueModuleName))
  .settings(
    name := "interop-commons-queue-manager",
    libraryDependencies ++= Dependencies.Jars.queueDependencies,
    sharedSettings
  )
  .dependsOn(utils)
  .setupBuildInfo

lazy val cqrs = project
  .in(file(cqrsModuleName))
  .settings(name := "interop-commons-cqrs", libraryDependencies ++= Dependencies.Jars.cqrsDependencies, sharedSettings)
  .dependsOn(utils)
  .setupBuildInfo

lazy val rateLimiter = project
  .in(file(rateLimiterModuleName))
  .settings(
    name := "interop-commons-rate-limiter",
    libraryDependencies ++= Dependencies.Jars.rateLimiterDependencies,
    sharedSettings
  )
  .dependsOn(utils)
  .setupBuildInfo

lazy val parser = project
  .in(file(parserModuleName))
  .settings(
    name := "interop-commons-parser",
    libraryDependencies ++= Dependencies.Jars.parserDependencies,
    sharedSettings
  )
  .setupBuildInfo

lazy val riskAnalysis = project
  .in(file(riskAnalysisModuleName))
  .settings(
    name := "interop-commons-risk-analysis",
    libraryDependencies ++= Dependencies.Jars.riskAnalysisDependencies,
    sharedSettings
  )
  .setupBuildInfo

lazy val commons = (project in file("."))
  .aggregate(utils, fileManager, mailManager, rateLimiter, signer, jwtModule, queue, cqrs, parser, riskAnalysis)
  .settings(name := "interop-commons")
  .enablePlugins(NoPublishPlugin)
