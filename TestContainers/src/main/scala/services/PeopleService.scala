package services

import com.mongodb.client.MongoClients
import config.ApplicationConfig
import models.Person
import repository.PersonUal
import zio.{ZIO, ZLayer}

object PeopleService {

  def savePerson(person: Person): ZIO[ApplicationConfig, Throwable, Boolean] = {
      PersonUal.insertOne(person)
  }

  def getPeople: ZIO[ApplicationConfig, Throwable, List[Person]] = {
      PersonUal.getAll
  }

}
