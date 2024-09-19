
import config.ApplicationConfig
import extensions.*
import models.Person
import repository.MongoDbClient
import services.PeopleService
import zio.http.*
import zio.json.*
import zio.{Scope, ZIO, ZIOAppArgs, ZIOAppDefault, ZLayer}

object main extends ZIOAppDefault {

  private val routes: Routes[MongoDbClient, Nothing] = {
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

  def run: ZIO[ZIOAppArgs & Scope, Any, Any] =  {
    Server.serve(routes)
      .provide(
        Server.default,
        ApplicationConfig.live >>> MongoDbClient.live
      )
  }

  private def savePerson(requestBody: Request): ZIO[MongoDbClient, Nothing, Response] = {
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

  private def getPeople(requestBody: Request): ZIO[MongoDbClient, Nothing, Response] = {
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
