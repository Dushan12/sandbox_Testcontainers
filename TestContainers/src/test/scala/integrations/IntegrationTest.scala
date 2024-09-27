package integrations

import com.dimafeng.testcontainers.GenericContainer
import com.dimafeng.testcontainers.GenericContainer.DockerImage
import com.mongodb.client.{MongoClient, MongoClients, MongoCollection}
import config.ApplicationConfig
import models.Person
import org.bson.Document
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.images.PullPolicy
import repository.ApplicationRepository
import services.PeopleService
import zio.test.{Spec, TestAspect, TestEnvironment, ZIOSpecDefault, assertTrue}
import zio.{Scope, ZIO, ZLayer}

object IntegrationTest extends ZIOSpecDefault {
  def spec: Spec[TestEnvironment & Scope, Any] = {

    suite("Integration -> TestContainers -> PeopleService -> getPeople -> Specs")(
      test("check save and pull elements from database") {

        val applicationRepositoryLayer = ZLayer.succeed {

          val container2def = GenericContainer.Def(
            "mongodb/mongodb-community-server:5.0.21-ubuntu2004",
            exposedPorts = Seq(27017),
            waitStrategy = Wait.defaultWaitStrategy(),
            imagePullPolicy = PullPolicy.defaultPolicy()
          )
          val container2 = container2def.start()


          val mongoUri = "mongodb://" + container2.host + ":" + container2.mappedPort(container2.exposedPorts.head)



          new ApplicationRepository {

            val client: MongoClient = MongoClients.create(mongoUri)

            val config: ApplicationConfig = new ApplicationConfig {
              val dbName: String = "testContainers"

              val peopleCollectionName: String = "people"

              val emailStatusCollectionName: String = "emailStatus"

              val databaseUrl: String = "mongodb://localhost:27017"
            }

            override def getPeopleCollection: MongoCollection[Document] = {
              client.getDatabase("testContainers").getCollection("people")
            }

            override def getEmailStatusCollection: MongoCollection[Document] = {
              client.getDatabase("testContainers").getCollection("emailStatus")
            }

          }
        }

        (for {
          peopleService <- ZIO.service[PeopleService]
          _ <- peopleService.savePerson(Person("1", "Dushan", "Gajik", "gajikdushan@gmail.com"))
          _ <- peopleService.savePerson(Person("2", "Dushan", "Gajik", "dushan.gajik@gmail.com"))
          result <- peopleService.getPeople
        } yield {
          assertTrue(result.head == Person("1", "Dushan", "Gajik", "gajikdushan@gmail.com"))
          assertTrue(result.last == Person("2", "Dushan", "Gajik", "dushan.gajik@gmail.com"))

        }).provide(
          applicationRepositoryLayer >>> PeopleService.live
        )
      }
    )
  }
}
