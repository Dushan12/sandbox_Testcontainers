package integrations

import com.dimafeng.testcontainers.GenericContainer
import com.dimafeng.testcontainers.GenericContainer.DockerImage
import com.mongodb.client.{MongoClient, MongoClients, MongoCollection}
import config.ApplicationConfig
import models.Person
import org.bson.Document
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.images.PullPolicy
import repository.PersonRepository
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
        (for {
          _ <- PeopleService.savePerson(Person("1", "Dushan", "Gajik", "gajikdushan@gmail.com"))
          _ <- PeopleService.savePerson(Person("2", "Dushan", "Gajik", "dushan.gajik@gmail.com"))
          result <- PeopleService.getPeople
        } yield {
          assertTrue(result.head == Person("1", "Dushan", "Gajik", "gajikdushan@gmail.com"))
          assertTrue(result.last == Person("2", "Dushan", "Gajik", "dushan.gajik@gmail.com"))

        }).provide(
          ZLayer.succeed(new PersonRepository {
            val client: MongoClient = MongoClients.create(getMongoContainer)

            val config: ApplicationConfig = new ApplicationConfig {
              def dbName: String = "testContainers"
              def peopleCollectionName: String  = "people"
              def emailStatusCollectionName: String  = "emailStatus"
              def databaseUrl: String = "mongodb://localhost:27017"
            }

            override def getPeopleCollection: MongoCollection[Document] = {
              client.getDatabase("testContainers").getCollection("people")
            }

            override def getEmailStatusCollection: MongoCollection[Document] = {
              client.getDatabase("testContainers").getCollection("emailStatus")
            }
          })
        )
      } @@ TestAspect.ignore
    )
  }
}
