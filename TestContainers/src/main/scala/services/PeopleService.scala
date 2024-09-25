package services

import models.Person
import repository.PersonRepository
import zio.ZIO

object PeopleService {

  def savePerson(person: Person): ZIO[PersonRepository, Throwable, Boolean] = {
    for {
      personUal <- ZIO.service[PersonRepository]
      insertOneResult <- personUal.insertOne(person)
    } yield {
      insertOneResult
    }
  }

  def getPeople: ZIO[PersonRepository, Throwable, List[Person]] = {
    for {
      personUal <- ZIO.service[PersonRepository]
      results <- personUal.getAll
    } yield {
      results
    }
  }

  def updatePerson(id: String, firstName: String): ZIO[PersonRepository, Nothing, Unit] = {
    for {
      personRepository <- ZIO.service[PersonRepository]
    } yield {
      personRepository.updatePersonAndStoreEmailRequest(id, firstName)
    }
  }

}
