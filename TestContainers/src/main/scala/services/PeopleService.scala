package services

import config.ApplicationConfig
import models.Person
import repository.PersonUal
import zio.ZIO

object PeopleService {

  def savePerson(person: Person): ZIO[PersonUal, Throwable, Boolean] = {
    for {
      personUal <- ZIO.service[PersonUal]
      insertOneResult <- personUal.insertOne(person)
    } yield {
      insertOneResult
    }
  }

  def getPeople: ZIO[PersonUal, Throwable, List[Person]] = {
    for {
      personUal <- ZIO.service[PersonUal]
      results <- personUal.getAll
    } yield {
      results
    }
  }

}
