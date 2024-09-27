
import config.ApplicationConfig
import extensions.*
import models.Person
import repository.ApplicationRepository
import services.{EmailService, PeopleService, RedisDatabase}
import zio.http.*
import zio.json.*
import zio.redis.{CodecSupplier, Redis}
import zio.{Duration, Schedule, ZIO, ZIOAppArgs, ZIOAppDefault, ZLayer}

import java.util.concurrent.TimeUnit

object main extends ZIOAppDefault {


  def run: ZIO[ZIOAppArgs, Any, Any] = {
    (for {
      emailService <- ZIO.service[EmailService]
      _ <- emailService.drainingQueueAndSendMessagesWithRetry.repeat(Schedule.fixed(Duration(1, TimeUnit.SECONDS))).forkDaemon
      _ <- Server.serve(routes)
    } yield ()).provide(
      Server.default,
      ZLayer.succeed[CodecSupplier](services.ProtobufCodecSupplier) >>> Redis.local,
      ApplicationConfig.live >>> RedisDatabase.live,
      ApplicationConfig.live >>> EmailService.live,
      ApplicationConfig.live >>> ApplicationRepository.live,
      ApplicationConfig.live >>> ApplicationRepository.live >>> PeopleService.live
    )
  }



  private val routes: Routes[PeopleService, Nothing] = {
    Routes(
      Method.POST / "people/save" -> handler {
        (req: Request) =>
          savePerson(req)
      },
      Method.GET / "people" -> handler { (req: Request) =>
        getPeople(req)
      }
    )
  }



  private def savePerson(requestBody: Request): ZIO[PeopleService, Nothing, Response] = {
    (for {
      person <- requestBody.asObject[Person]
      peopleService <- ZIO.service[PeopleService]
      savedPersonResult <- peopleService.savePerson(person)
      response <- ZIO.succeed({
        if (savedPersonResult)
          Response.text("Person saved successfully!")
        else
          Response.error(
            zio.http.Status.InternalServerError,
            s"""Failed to save person in database!"""
          )
      })
    } yield (response))
      .catchAll { error =>
        ZIO.succeed(Response.error(
          zio.http.Status.InternalServerError,
          s"""Error while saving person to database: $error"""
        ))
      }
  }

  private def getPeople(requestBody: Request): ZIO[PeopleService, Nothing, Response] = {
    (for {
      peopleService <- ZIO.service[PeopleService]
      personResults <- peopleService.getPeople
    } yield {
      Response.json(personResults.map(_.toJson).toJson.mkString(""))
    }).catchAll { error =>
      ZIO.succeed(Response.error(
        zio.http.Status.InternalServerError,
        s"""Error while saving person to database: $error"""
      ))
    }
  }

}
