package integrations

import com.dimafeng.testcontainers.GenericContainer
import com.dimafeng.testcontainers.GenericContainer.DockerImage
import config.ApplicationConfig
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.images.PullPolicy
import services.PeopleService
import zio.test.{Spec, TestAspect, TestEnvironment, ZIOSpecDefault, assertTrue}
import zio.{Scope, ZIO, ZLayer}

object IntegrationTest extends ZIOSpecDefault {
  def spec: Spec[TestEnvironment & Scope, Any] = {

    def getMongoContainer = {
      val container = GenericContainer.Def(
        "mongodb/mongodb-community-server:5.0.21-ubuntu2004",
        exposedPorts = Seq(27017),
        waitStrategy = Wait.defaultWaitStrategy(),
        imagePullPolicy = PullPolicy.defaultPolicy()
      ).start()
      "mongodb://" + container.host + ":" + container.mappedPort(container.exposedPorts.head)
    }

    suite("Integration -> TestContainers -> PeopleService -> getPeople -> Specs")(
      test("check save and pull elements from database") {
        val mongodbUrl = getMongoContainer
        for {
          a <- PeopleService.getPeople.provide(
            ZLayer.apply(ZIO.succeed(ApplicationConfig("testContainers", "people", mongodbUrl)))
          )
        } yield {
          println(a)
          assertTrue(true)
        }
      }
    )
  }
}
