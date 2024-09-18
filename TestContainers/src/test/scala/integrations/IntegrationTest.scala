package integrations

import com.dimafeng.testcontainers.GenericContainer
import com.dimafeng.testcontainers.GenericContainer.DockerImage
import com.mongodb.client.MongoClients
import config.ApplicationConfig
import models.Person
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.images.PullPolicy
import services.PeopleService
import zio.test.{Spec, TestEnvironment, ZIOSpecDefault, assertTrue}
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
        (for {
          _ <- PeopleService.savePerson(Person("Dushan", "Gajik", "gajikdushan@gmail.com"))
          _ <- PeopleService.savePerson(Person("Dushan", "Gajik", "dushan.gajik@gmail.com"))
          result <- PeopleService.getPeople
        } yield {
          assertTrue(result.head == Person("Dushan", "Gajik", "gajikdushan@gmail.com"))
          assertTrue(result.last == Person("Dushan", "Gajik", "dushan.gajik@gmail.com"))
        }).provide(
          ZLayer.apply(ZIO.succeed(ApplicationConfig("testContainers", "people", mongodbUrl))),
          ZLayer.apply(ZIO.succeed(MongoClients.create(mongodbUrl)))
        )
      }
    )
  }
}
