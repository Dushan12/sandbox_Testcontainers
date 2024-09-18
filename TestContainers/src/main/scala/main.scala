
import com.mongodb.client.{MongoClient, MongoClients}
import config.ApplicationConfig
import models.Person
import services.PeopleService
import zio.http.*
import zio.json.*
import zio.{Ref, Scope, ZIO, ZIOAppArgs, ZIOAppDefault, ZLayer}

object main extends ZIOAppDefault {

  private val routes: Routes[Any, Nothing] = {
    val appConfig = ApplicationConfig("testContainers", "people", "mongodb://localhost:27017")
    val mongoClient = MongoClients.create("mongodb://localhost:27017")
    Routes(
        Method.POST / "people/save" -> handler {
          (req: Request) =>
            savePerson(req)
              .provide(
                ZLayer.apply(ZIO.succeed(appConfig)),
                ZLayer.apply(ZIO.succeed(mongoClient))
              )
        },
        Method.GET / "people" -> handler { (req: Request) => getPeople(req)
          .provide(
            ZLayer.apply(ZIO.succeed(appConfig)),
            ZLayer.apply(ZIO.succeed(mongoClient))
          ) }
      )
  }

  def run: ZIO[ZIOAppArgs & Scope, Any, Any] =  {
    Server.serve(routes).provide(Server.default)

  }

  private def savePerson(req: Request): ZIO[ApplicationConfig & MongoClient, Nothing, Response] = {

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

  private def getPeople(req: Request): ZIO[ApplicationConfig & MongoClient, Nothing, Response] = {
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
