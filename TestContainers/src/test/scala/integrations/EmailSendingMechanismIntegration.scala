package integrations

import com.dimafeng.testcontainers.GenericContainer
import com.mongodb.client.{MongoClient, MongoClients, MongoCollection}
import config.ApplicationConfig
import integrations.IntegrationTest.{suite, test}
import models.{EmailStatus, Person}
import org.bson.Document
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.images.PullPolicy
import repository.ApplicationRepository
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

    val redisDatabaseLayer = ZLayer.succeed {
      val container1def = GenericContainer.Def(
        "redis/redis-stack-server:7.4.0-v0",
        exposedPorts = Seq(6379), //docker run -d --name redis-stack-server -p 6379:
        waitStrategy = Wait.forListeningPort(),
        imagePullPolicy = PullPolicy.defaultPolicy()
      )
      val container1 = container1def.start()
      val redisPort = container1.mappedPort(container1.exposedPorts.head)

      new RedisConfig(

        host = "localhost",
        port = redisPort,
      )
    }

    val applicationRepositoryLayer = ZLayer.succeed(new ApplicationRepository {



      val container2def = GenericContainer.Def(
        "mongodb/mongodb-community-server:5.0.21-ubuntu2004",
        exposedPorts = Seq(27017),
        waitStrategy = Wait.defaultWaitStrategy(),
        imagePullPolicy = PullPolicy.defaultPolicy()
      )
      val container2 = container2def.start()


      val mongoUri = "mongodb://" + container2.host + ":" + container2.mappedPort(container2.exposedPorts.head)

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

    })

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




        (for {
          peopleService <- ZIO.service[PeopleService]
          emailService <- ZIO.service[EmailService]
          _ <- peopleService.savePerson(Person("1", "Dusan", "Gajik", "gajikdushan@gmail.com"))
          _ <- peopleService.savePerson(Person("2", "Dusan", "Gajik", "gajikdushan@gmail.com"))
          _ <- peopleService.savePerson(Person("3", "Dusan", "Gajik", "gajikdushan@gmail.com"))
          _ <- peopleService.savePerson(Person("4", "Dusan", "Gajik", "gajikdushan@gmail.com"))
          _ <- peopleService.updatePerson("1", "Dushan")
          _ <- peopleService.updatePerson("2", "Dushan")
          _ <- peopleService.updatePerson("3", "Dushan")
          _ <- peopleService.updatePerson("4", "Dushan")
          result <- peopleService.getPeople
          results <- emailService.drainingQueueAndSendMessagesWithRetry
          resultsAfter <- emailService.drainingQueueAndSendMessagesWithRetry
        } yield {
          assertTrue(result.head == Person("1", "Dushan", "Gajik", "gajikdushan@gmail.com"))
          assertTrue(result.length == 1)
          assertTrue(results == 4)
          assertTrue(resultsAfter == 0)
        }).provide(
          ApplicationConfig.live,
          redisDatabaseLayer,
          ZLayer.succeed[CodecSupplier](ProtobufCodecSupplier) >>> Redis.singleNode,
          RedisDatabase.live,
          ZLayer.service[ApplicationConfig].project(config => mockEmailServiceSucceed),
          applicationRepositoryLayer,
          applicationRepositoryLayer >>> PeopleService.live

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
          peopleService <- ZIO.service[PeopleService]
          emailService <- ZIO.service[EmailService]
          _ <- peopleService.savePerson(Person("1", "Dusan", "Gajik", "gajikdushan@gmail.com"))
          _ <- peopleService.savePerson(Person("2", "Dusan", "Gajik", "gajikdushan@gmail.com"))
          _ <- peopleService.savePerson(Person("3", "Dusan", "Gajik", "gajikdushan@gmail.com"))
          _ <- peopleService.savePerson(Person("4", "Dusan", "Gajik", "gajikdushan@gmail.com"))
          _ <- peopleService.updatePerson("1", "Dushan")
          _ <- peopleService.updatePerson("2", "Dushan")
          _ <- peopleService.updatePerson("3", "Dushan")
          _ <- peopleService.updatePerson("4", "Dushan")
          result <- peopleService.getPeople
          results <- emailService.drainingQueueAndSendMessagesWithRetry
          resultsAfter <- emailService.drainingQueueAndSendMessagesWithRetry
        } yield {
          container1.stop()
          container2.stop()
          assertTrue(result.head == Person("1", "Dushan", "Gajik", "gajikdushan@gmail.com"))
          assertTrue(result.length == 1)
          assertTrue(results == 0)
          assertTrue(resultsAfter == 0)
        }).provide(
          ApplicationConfig.live,
          redisDatabaseLayer,
          ZLayer.succeed[CodecSupplier](ProtobufCodecSupplier) >>> Redis.singleNode,
          RedisDatabase.live,
          ZLayer.service[ApplicationConfig].project(config => mockEmailServiceFailed),
          applicationRepositoryLayer,
          applicationRepositoryLayer >>> PeopleService.live
        )
      }
    )
  }
}
