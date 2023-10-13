import OpenApiTools._

ThisBuild / scalaVersion      := "2.13.10"
ThisBuild / organization      := "it.pagopa"
ThisBuild / organizationName  := "Pagopa S.p.A."
Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val sharedSettings: SettingsDefinition =
  Seq(publish / skip := true, publish := (()), publishLocal := (()), publishTo := None, Docker / publish := {})

val generateCode = taskKey[Unit]("A task for generating code starting from the swagger definition")
generateCode := {
  println("Generating Code from OpenApi specs...")
  generateClientProcess("catalog-management", "catalogmanagement")
  generateServerProcess("catalog-management", "catalogmanagement")
  generateClientProcess("catalog-process", "catalogprocess")
  generateServerProcess("catalog-process", "catalogprocess")
  println("Code from OpenApi specs completed")

}

(Compile / compile) := ((Compile / compile) dependsOn generateCode).value

lazy val commons           = RootProject(file("commons"))
lazy val catalogManagement = RootProject(file("services/catalog-management"))
lazy val catalogProcess    = RootProject(file("services/catalog-process"))

lazy val platform = (project in file("."))
  .settings(sharedSettings)
  .aggregate(commons, catalogManagement, catalogProcess)
  .enablePlugins(NoPublishPlugin)
