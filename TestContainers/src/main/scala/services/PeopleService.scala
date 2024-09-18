package services

import config.ApplicationConfig
import models.Person
import repository.{MongoDbClient, PersonUal}
import zio.ZIO

object PeopleService {

  def savePerson(person: Person): ZIO[ApplicationConfig & MongoDbClient, Throwable, Boolean] = {
      PersonUal.insertOne(person)
  }

  def getPeople: ZIO[ApplicationConfig & MongoDbClient, Throwable, List[Person]] = {
      PersonUal.getAll
  }

}
