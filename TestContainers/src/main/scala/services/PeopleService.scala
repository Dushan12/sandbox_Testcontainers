package services

import com.mongodb.client.{MongoClient, MongoClients}
import config.ApplicationConfig
import models.Person
import repository.{MongoDbClient, PersonUal}
import zio.{ZIO, ZLayer}

object PeopleService {

  def savePerson(person: Person): ZIO[ApplicationConfig & MongoDbClient, Throwable, Boolean] = {
      PersonUal.insertOne(person)
  }

  def getPeople: ZIO[ApplicationConfig & MongoDbClient, Throwable, List[Person]] = {
      PersonUal.getAll
  }

}
