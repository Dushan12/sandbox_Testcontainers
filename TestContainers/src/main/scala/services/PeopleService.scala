package services

import config.ApplicationConfig
import models.Person
import repository.{MongoDbClient, PersonUal}
import zio.ZIO

object PeopleService {

  def savePerson(person: Person): ZIO[MongoDbClient, Throwable, Boolean] = {
      PersonUal.insertOne(person)
  }

  def getPeople: ZIO[MongoDbClient, Throwable, List[Person]] = {
      PersonUal.getAll
  }

}
