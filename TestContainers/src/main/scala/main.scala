
import com.mongodb.client.{MongoClient, MongoClients}
import config.ApplicationConfig
import models.Person
import repository.MongoDbClient
import services.PeopleService
import zio.http.*
import zio.json.*
import zio.{Ref, Scope, URIO, ZIO, ZIOAppArgs, ZIOAppDefault, ZLayer}

object main extends ZIOAppDefault {

  private val routes: Routes[ApplicationConfig & MongoDbClient, Nothing] = {
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

  def application: URIO[ApplicationConfig & MongoDbClient & Server, Nothing] = Server.serve(routes)

  def run: ZIO[ZIOAppArgs & Scope, Any, Any] =  {
   // val appConfig = ApplicationConfig("testContainers", "people", "mongodb://localhost:27017")
    val mongoClient = MongoClients.create("mongodb://localhost:27017")
    application
      .provide(
      Server.default,
        ApplicationConfig.live,
        MongoDbClient.live
      )
  }

  private def savePerson(req: Request): ZIO[ApplicationConfig & MongoDbClient, Nothing, Response] = {

    (for {
      bodyStr <- req.body.asString.mapError(_.getMessage)
      parseBodyAsPerson <- ZIO.fromEither(bodyStr.fromJson[Person])
      savedPersonResult <- PeopleService.savePerson(parseBodyAsPerson)
    } yield {
      if(savedPersonResult)
        Response.text("Person saved successfully!")
      else
        Response.error(
          zio.http.Status.InternalServerError,
          s"""Failed to save person in database!"""
        )
    }).catchAll { error =>
      ZIO.succeed(Response.error(
        zio.http.Status.InternalServerError,
        s"""Error while saving person to database: $error"""
      ))
    }
  }

  private def getPeople(req: Request): ZIO[ApplicationConfig & MongoDbClient, Nothing, Response] = {
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
