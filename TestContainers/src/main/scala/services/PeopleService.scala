package services

import com.mongodb.client.{MongoClient, MongoClients}
import config.ApplicationConfig
import models.Person
import repository.PersonUal
import zio.{ZIO, ZLayer}

object PeopleService {

  def savePerson(person: Person): ZIO[ApplicationConfig & MongoClient, Throwable, Boolean] = {
      PersonUal.insertOne(person)
  }

  def getPeople: ZIO[ApplicationConfig & MongoClient, Throwable, List[Person]] = {
      PersonUal.getAll
  }

}
