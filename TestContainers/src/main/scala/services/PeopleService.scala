package services

import com.mongodb.client.{MongoClient, MongoClients}
import models.Person
import repository.PersonUal
import zio.{ZIO, ZLayer}

object PeopleService {


  private def getClient(): ZIO[Any, Throwable, MongoClient] = {
    ZIO.attempt(MongoClients.create("mongodb://localhost:27017"))
  }


  def savePerson(person: Person): ZIO[Any, Throwable, Boolean] = {
    (for {
      client <- ZIO.service[MongoClient]
      res <- PersonUal.insertOne(person)
    } yield {
      res
    }).provide(ZLayer.apply(getClient()))
  }

  def getPeople(): List[Person] = {
    throw new NotImplementedError()
  }

}
