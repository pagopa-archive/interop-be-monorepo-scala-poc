
ThisBuild / scalaVersion      := "2.13.10"
ThisBuild / organization      := "it.pagopa"
ThisBuild / organizationName  := "Pagopa S.p.A."
Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val sharedSettings: SettingsDefinition =
  Seq(publish / skip := true, publish := (()), publishLocal := (()), publishTo := None, Docker / publish := {})

lazy val commons         = RootProject(file("commons"))
lazy val selfcareClients = RootProject(file("selfcare-proxy-clients"))

lazy val agreementManagement         = RootProject(file("services/agreement-management"))
lazy val agreementProcess            = RootProject(file("services/agreement-process"))
lazy val apiGatewayProcess           = RootProject(file("services/api-gateway"))
lazy val attributeRegistryManagement = RootProject(file("services/attribute-registry-management"))
lazy val attributeRegistryProcess    = RootProject(file("services/attribute-registry-process"))
lazy val authorizationManagement     = RootProject(file("services/authorization-management"))
lazy val authorizationProcess        = RootProject(file("services/authorization-process"))
lazy val authorizationServer         = RootProject(file("services/authorization-server"))
lazy val backendForFrontend          = RootProject(file("services/backend-for-frontend"))
lazy val catalogManagement           = RootProject(file("services/catalog-management"))
lazy val catalogProcess              = RootProject(file("services/catalog-process"))
lazy val notifier                    = RootProject(file("services/notifier"))
lazy val partyRegistryProxy          = RootProject(file("services/party-registry-proxy"))
lazy val purposeManagement           = RootProject(file("services/purpose-management"))
lazy val purposeProcess              = RootProject(file("services/purpose-process"))
lazy val tenantManagement            = RootProject(file("services/tenant-management"))
lazy val tenantProcess               = RootProject(file("services/tenant-process"))

lazy val platform = (project in file("."))
  .settings(sharedSettings)
  .aggregate(
    commons,
    selfcareClients,
    agreementManagement,
    agreementProcess,
    apiGatewayProcess,
    attributeRegistryManagement,
    attributeRegistryProcess,
    authorizationManagement,
    authorizationProcess,
    authorizationServer,
    backendForFrontend,
    catalogManagement,
    catalogProcess,
    notifier,
    partyRegistryProxy,
    purposeManagement,
    purposeProcess,
    tenantManagement,
    tenantProcess
  )
  .enablePlugins(NoPublishPlugin)
