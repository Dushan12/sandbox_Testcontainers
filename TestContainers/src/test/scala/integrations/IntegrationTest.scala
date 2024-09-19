package integrations

import com.dimafeng.testcontainers.GenericContainer
import com.dimafeng.testcontainers.GenericContainer.DockerImage
import com.mongodb.client.{MongoClient, MongoClients, MongoCollection}
import models.Person
import org.bson.Document
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.images.PullPolicy
import repository.MongoDbClient
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
        (for {
          _ <- PeopleService.savePerson(Person("Dushan", "Gajik", "gajikdushan@gmail.com"))
          _ <- PeopleService.savePerson(Person("Dushan", "Gajik", "dushan.gajik@gmail.com"))
          result <- PeopleService.getPeople
        } yield {
          assertTrue(result.head == Person("Dushan", "Gajik", "gajikdushan@gmail.com"))
          assertTrue(result.last == Person("Dushan", "Gajik", "dushan.gajik@gmail.com"))

        }).provide(
          ZLayer.succeed(new MongoDbClient {
            val client: MongoClient = MongoClients.create(getMongoContainer)

            override def getPeopleCollection: MongoCollection[Document] = {
              client.getDatabase("testContainers").getCollection("people")
            }
          })
        )
      }
    )
  }
}
