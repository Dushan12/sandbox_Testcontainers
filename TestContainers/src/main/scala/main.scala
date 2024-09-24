
import config.ApplicationConfig
import extensions.*
import models.Person
import repository.PersonRepository
import services.{EmailService, PeopleService, RedisDatabase}
import zio.ExecutionStrategy.Parallel
import zio.http.*
import zio.json.*
import zio.{Duration, ExecutionStrategy, Schedule, Scope, ZIO, ZIOAppArgs, ZIOAppDefault, ZLayer}

import java.time.temporal.ChronoUnit

object main extends ZIOAppDefault {

  private val routes: Routes[PersonRepository, Nothing] = {
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

  def run: ZIO[ZIOAppArgs, Any, Any] =  {
    (for {
      emailService <- ZIO.service[EmailService]
      scope <- ZIO.service[Scope]
      _ <- emailService.drainingQueueAndSendMessagesWithRetry.repeat(Schedule.fixed(Duration.apply(1, ChronoUnit.MINUTES))).forkIn(scope)
      _ <- Server.serve(routes)
    } yield ()).provide(
        Server.default,
        Scope.default,
        ApplicationConfig.live >>> RedisDatabase.live,
        ApplicationConfig.live >>> EmailService.live,
        ApplicationConfig.live >>> PersonRepository.live
      )
  }

  private def savePerson(requestBody: Request): ZIO[PersonRepository, Nothing, Response] = {
    (for {
      person <- requestBody.asObject[Person]
      savedPersonResult <- PeopleService.savePerson(person)
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

  private def getPeople(requestBody: Request): ZIO[PersonRepository, Nothing, Response] = {
    (for {
      personResults <- PeopleService.getPeople
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
