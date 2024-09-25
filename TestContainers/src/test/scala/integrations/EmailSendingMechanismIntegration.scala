package integrations

import com.dimafeng.testcontainers.GenericContainer
import com.mongodb.client.{MongoClient, MongoClients, MongoCollection}
import config.ApplicationConfig
import integrations.IntegrationTest.{suite, test}
import models.{EmailStatus, Person}
import org.bson.Document
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.images.PullPolicy
import repository.PersonRepository
import services.{EmailService, PeopleService, RedisDatabase}
import zio.redis.{CodecSupplier, Redis, RedisConfig}
import zio.schema.Schema
import zio.schema.codec.{BinaryCodec, ProtobufCodec}
import zio.test.{Spec, TestEnvironment, ZIOSpecDefault, assertTrue}
import zio.{Scope, ZIO, ZLayer}

object EmailSendingMechanismIntegration extends ZIOSpecDefault {

  private object ProtobufCodecSupplier extends CodecSupplier {
    def get[A: Schema]: BinaryCodec[A] = ProtobufCodec.protobufCodec
  }

  def spec: Spec[TestEnvironment & Scope, Any] = {

    val mockEmailServiceSucceed = new EmailService {
      override def sendEmail(emailStatus: EmailStatus): ZIO[Any, Throwable, Unit] = {
        ZIO.succeed(())
      }
    }

    val mockEmailServiceFailed = new EmailService {
      override def sendEmail(emailStatus: EmailStatus): ZIO[Any, Throwable, Unit] = {
        ZIO.fail(new Exception("FAILED TO SEND MAIL"))
      }
    }

    suite("Integration -> TestContainers -> EmailService -> sendEmail -> Specs")(
      test("update element and schedule mail for sending, then send the mail and return result of successful sending") {


        val container1def = GenericContainer.Def(
          "redis/redis-stack-server:7.4.0-v0",
          exposedPorts = Seq(6379), //docker run -d --name redis-stack-server -p 6379:
          waitStrategy = Wait.forListeningPort(),
          imagePullPolicy = PullPolicy.defaultPolicy()
        )
        val container1 = container1def.start()
        val redisPort = container1.mappedPort(container1.exposedPorts.head)

        val container2def = GenericContainer.Def(
          "mongodb/mongodb-community-server:5.0.21-ubuntu2004",
          exposedPorts = Seq(27017),
          waitStrategy = Wait.defaultWaitStrategy(),
          imagePullPolicy = PullPolicy.defaultPolicy()
        )
        val container2 = container2def.start()


        val mongoUri = "mongodb://" + container2.host + ":" + container2.mappedPort(container2.exposedPorts.head)

          (for {
            emailService <- ZIO.service[EmailService]
            _ <- PeopleService.savePerson(Person("1", "Dusan", "Gajik", "gajikdushan@gmail.com"))
            _ <- PeopleService.savePerson(Person("2", "Dusan", "Gajik", "gajikdushan@gmail.com"))
            _ <- PeopleService.savePerson(Person("3", "Dusan", "Gajik", "gajikdushan@gmail.com"))
            _ <- PeopleService.savePerson(Person("4", "Dusan", "Gajik", "gajikdushan@gmail.com"))
            _ <- PeopleService.updatePerson("1", "Dushan")
            _ <- PeopleService.updatePerson("2", "Dushan")
            _ <- PeopleService.updatePerson("3", "Dushan")
            _ <- PeopleService.updatePerson("4", "Dushan")
            result <- PeopleService.getPeople
            results <- emailService.drainingQueueAndSendMessagesWithRetry
          } yield {
            container1.stop()
            container2.stop()
            assertTrue(result.head == Person("1", "Dushan", "Gajik", "gajikdushan@gmail.com"))
            assertTrue(result.length == 1)
            assertTrue(results == 4)
          }).provide(
            ApplicationConfig.live,
            ZLayer.succeed(new RedisConfig(
              host = "localhost",
              port = redisPort,
            )),
            ZLayer.succeed[CodecSupplier](ProtobufCodecSupplier) >>> Redis.singleNode,
            RedisDatabase.live,
            ZLayer.service[ApplicationConfig].project(config => mockEmailServiceSucceed),
            ZLayer.succeed(new PersonRepository {


              val client: MongoClient = MongoClients.create(mongoUri)

              val config: ApplicationConfig = new ApplicationConfig {
                def dbName: String = "testContainers"

                def peopleCollectionName: String = "people"

                def emailStatusCollectionName: String = "emailStatus"

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
      },
      test("update element and schedule mail sending, then send the mail and return 0 because of failure") {



        val container1def = GenericContainer.Def(
          "redis/redis-stack-server:7.4.0-v0",
          exposedPorts = Seq(6379), //docker run -d --name redis-stack-server -p 6379:
          waitStrategy = Wait.forListeningPort(),
          imagePullPolicy = PullPolicy.defaultPolicy()
        )
        val container1 = container1def.start()
        val redisPort = container1.mappedPort(container1.exposedPorts.head)

        val container2def = GenericContainer.Def(
          "mongodb/mongodb-community-server:5.0.21-ubuntu2004",
          exposedPorts = Seq(27017),
          waitStrategy = Wait.defaultWaitStrategy(),
          imagePullPolicy = PullPolicy.defaultPolicy()
        )
        val container2 = container2def.start()


        val mongoUri = "mongodb://" + container2.host + ":" + container2.mappedPort(container2.exposedPorts.head)

        (for {
          emailService <- ZIO.service[EmailService]
          _ <- PeopleService.savePerson(Person("1", "Dusan", "Gajik", "gajikdushan@gmail.com"))
          _ <- PeopleService.savePerson(Person("2", "Dusan", "Gajik", "gajikdushan@gmail.com"))
          _ <- PeopleService.savePerson(Person("3", "Dusan", "Gajik", "gajikdushan@gmail.com"))
          _ <- PeopleService.savePerson(Person("4", "Dusan", "Gajik", "gajikdushan@gmail.com"))
          _ <- PeopleService.updatePerson("1", "Dushan")
          _ <- PeopleService.updatePerson("2", "Dushan")
          _ <- PeopleService.updatePerson("3", "Dushan")
          _ <- PeopleService.updatePerson("4", "Dushan")
          result <- PeopleService.getPeople
          results <- emailService.drainingQueueAndSendMessagesWithRetry
        } yield {
          container1.stop()
          container2.stop()
          assertTrue(result.head == Person("1", "Dushan", "Gajik", "gajikdushan@gmail.com"))
          assertTrue(result.length == 1)
          assertTrue(results == 0)
        }).provide(
          ApplicationConfig.live,
          ZLayer.succeed(new RedisConfig(
            host = "localhost",
            port = redisPort,
          )),
          ZLayer.succeed[CodecSupplier](ProtobufCodecSupplier) >>> Redis.singleNode,
          RedisDatabase.live,
          ZLayer.service[ApplicationConfig].project(config => mockEmailServiceFailed),
          ZLayer.succeed(new PersonRepository {


            val client: MongoClient = MongoClients.create(mongoUri)

            val config: ApplicationConfig = new ApplicationConfig {
              def dbName: String = "testContainers"

              def peopleCollectionName: String = "people"

              def emailStatusCollectionName: String = "emailStatus"

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
      }
    )
  }
}
