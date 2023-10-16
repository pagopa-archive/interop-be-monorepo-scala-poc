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
  generateClientProcess("agreement-management", "interop.agreementmanagement")
  generateServerProcess("agreement-management", "interop.agreementmanagement")
  generateClientProcess("agreement-process", "interop.agreementprocess")
  generateServerProcess("agreement-process", "interop.agreementprocess")

  generateServerProcess("api-gateway", "interop.apigateway")

  generateClientProcess("attribute-registry-management", "interop.attributeregistrymanagement")
  generateServerProcess("attribute-registry-management", "interop.attributeregistrymanagement")
  generateClientProcess("attribute-registry-process", "interop.attributeregistryprocess")
  generateServerProcess("attribute-registry-process", "interop.attributeregistryprocess")

  generateClientProcess("authorization-management", "interop.authorizationmanagement")
  generateServerProcess("authorization-management", "interop.authorizationmanagement")
  generateClientProcess("authorization-process", "interop.authorizationprocess")
  generateServerProcess("authorization-process", "interop.authorizationprocess")

  generateClientProcess("authorization-server", "interop.authorizationserver")
  generateServerProcess("authorization-server", "interop.authorizationserver")

  generateServerProcess("backend-for-frontend", "interop.backendforfrontend")

  generateClientProcess("catalog-management", "interop.catalogmanagement")
  generateServerProcess("catalog-management", "interop.catalogmanagement")
  generateClientProcess("catalog-process", "interop.catalogprocess")
  generateServerProcess("catalog-process", "interop.catalogprocess")

  generateClientProcess("notifier", "interop.notifier")
  generateServerProcess("notifier", "interop.notifier")

  generateClientProcess("party-registry-proxy", "interop.partyregistryproxy")
  generateServerProcess("party-registry-proxy", "interop.partyregistryproxy")

  generateClientProcess("purpose-management", "interop.purposemanagement")
  generateServerProcess("purpose-management", "interop.purposemanagement")
  generateClientProcess("purpose-process", "interop.purposeprocess")
  generateServerProcess("purpose-process", "interop.purposeprocess")

  generateSelfcareClientProcess("party-management-client", "interop.selfcare.partymanagement")
  generateSelfcareClientProcess("party-process-client", "interop.selfcare.partyprocess")
  generateSelfcareClientProcess("selfcare-v2-client", "interop.selfcare.v2")
  generateSelfcareClientProcess("user-registry-client", "interop.selfcare.userregistry")

  generateClientProcess("tenant-management", "interop.tenantmanagement")
  generateServerProcess("tenant-management", "interop.tenantmanagement")
  generateClientProcess("tenant-process", "interop.tenantprocess")
  generateServerProcess("tenant-process", "interop.tenantprocess")

  println("Code from OpenApi specs completed")

}

//(Compile / compile) := ((Compile / compile) dependsOn generateCode).value

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
