package services

import com.mongodb.client.{MongoClient, MongoClients}
import config.ApplicationConfig
import models.Person
import repository.PersonUal
import zio.{ZEnvironment, ZIO, ZLayer}

object PeopleService {

  def savePerson(person: Person): ZIO[Any, Throwable, Boolean] = {
    (for {
      client <- ZIO.service[MongoClient]
      applicationConfig <- ZIO.service[ApplicationConfig]
      res <- PersonUal.insertOne(person)
    } yield {
      res
    }).provide(
      ZLayer.apply(ZIO.attempt(MongoClients.create("mongodb://localhost:27017"))),
      ZLayer.apply(ZIO.succeed(ApplicationConfig("testContainers", "people")))
    )
  }

  def getPeople(): ZIO[Any, Throwable, List[Person]] = {
    (for {
      client <- ZIO.service[MongoClient]
      applicationConfig <- ZIO.service[ApplicationConfig]
      res <- PersonUal.getAll
    } yield {
      res
    }).provide(
      ZLayer.apply(ZIO.attempt(MongoClients.create("mongodb://localhost:27017"))),
      ZLayer.apply(ZIO.succeed(ApplicationConfig("testContainers", "people")))
    )
  }

}
