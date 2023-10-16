import scala.sys.process._

object OpenApiTools {

  def generateServerProcess(projectName: String, packagePrefix: String): Unit =
    Process(s"""openapi-generator-cli generate -t services/$projectName/template/scala-akka-http-server
               |                               -i services/$projectName/src/main/resources/interface-specification.yml
               |                               -g scala-akka-http-server
               |                               -p projectName=$projectName
               |                               -p invokerPackage=it.pagopa.$packagePrefix.server
               |                               -p modelPackage=it.pagopa.$packagePrefix.model
               |                               -p apiPackage=it.pagopa.$packagePrefix.api
               |                               -p dateLibrary=java8
               |                               -p entityStrictnessTimeout=15
               |                               -o services/$projectName/generated""".stripMargin).!!

  def generateClientProcess(projectName: String, packagePrefix: String): Unit =
    Process(s"""openapi-generator-cli generate -t services/$projectName/template/scala-akka-http-client
               |                               -i services/$projectName/src/main/resources/interface-specification.yml
               |                               -g scala-akka
               |                               -p projectName=$projectName
               |                               -p invokerPackage=it.pagopa.$packagePrefix.client.invoker
               |                               -p modelPackage=it.pagopa.$packagePrefix.client.model
               |                               -p apiPackage=it.pagopa.$packagePrefix.client.api
               |                               -p dateLibrary=java8
               |                               -o services/$projectName/client""".stripMargin).!!


  def generateSelfcareClientProcess(projectName: String, packagePrefix: String): Unit =
    Process(s"""openapi-generator-cli generate -t selfcare-proxy-clients/template/scala-akka-http-client
               |                               -i selfcare-proxy-clients/$projectName/interface-specification.yml
               |                               -g scala-akka
               |                               -p projectName=$projectName
               |                               -p invokerPackage=it.pagopa.$packagePrefix.client.invoker
               |                               -p modelPackage=it.pagopa.$packagePrefix.client.model
               |                               -p apiPackage=it.pagopa.$packagePrefix.client.api
               |                               -p dateLibrary=java8
               |                               -o selfcare-proxy-clients/$projectName""".stripMargin).!!




  //  def generateServerProcess(projectName: String, packagePrefix: String): Unit =
//    generateGenericServerProcess(s"services/$projectName", "services", projectName, packagePrefix)
//
//  def generateClientProcess(projectName: String, packagePrefix: String): Unit =
//    generateGenericClientProcess(s"services/$projectName", "services", projectName, packagePrefix)

  //  def generateGenericServerProcess(
//    templatePath: String,
//    path: String,
//    projectName: String,
//    packagePrefix: String
//  ): Unit =
//    Process(s"""openapi-generator-cli generate -t $templatePath/template/scala-akka-http-server
//               |                               -i $path/$projectName/src/main/resources/interface-specification.yml
//               |                               -g scala-akka-http-server
//               |                               -p projectName=$projectName
//               |                               -p invokerPackage=it.pagopa.$packagePrefix.server
//               |                               -p modelPackage=it.pagopa.$packagePrefix.model
//               |                               -p apiPackage=it.pagopa.$packagePrefix.api
//               |                               -p dateLibrary=java8
//               |                               -p entityStrictnessTimeout=15
//               |                               -o $path/$projectName/generated""".stripMargin).!!
//
//  def generateGenericClientProcess(
//    templatePath: String,
//    path: String,
//    projectName: String,
//    packagePrefix: String
//  ): Unit =
//    Process(s"""openapi-generator-cli generate -t $templatePath/template/scala-akka-http-client
//               |                               -i $path/$projectName/src/main/resources/interface-specification.yml
//               |                               -g scala-akka
//               |                               -p projectName=$projectName
//               |                               -p invokerPackage=it.pagopa.$packagePrefix.client.invoker
//               |                               -p modelPackage=it.pagopa.$packagePrefix.client.model
//               |                               -p apiPackage=it.pagopa.$packagePrefix.client.api
//               |                               -p dateLibrary=java8
//               |                               -o $path/$projectName/client""".stripMargin).!!
//
//  def generateServerProcess(projectName: String, packagePrefix: String): Unit =
//    generateGenericServerProcess(s"services/$projectName", "services", projectName, packagePrefix)
//
//  def generateClientProcess(projectName: String, packagePrefix: String): Unit =
//    generateGenericClientProcess(s"services/$projectName", "services", projectName, packagePrefix)

}
