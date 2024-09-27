package services

import config.ApplicationConfig
import models.Person
import repository.ApplicationRepository
import zio.{ZIO, ZLayer}

trait PeopleService {

  val personRepository: ApplicationRepository

  def savePerson(person: Person): ZIO[Any, Throwable, Boolean] = {
    personRepository.insertOne(person)
  }

  def getPeople: ZIO[Any, Throwable, List[Person]] = {
    personRepository.getAll
  }

  def updatePerson(id: String, firstName: String): ZIO[Any, Throwable, Unit] = {
    personRepository.updatePersonAndStoreEmailRequest(id, firstName)
  }

}


object PeopleService {
  val live: ZLayer[ApplicationRepository, Nothing, PeopleService] = {
    ZLayer.service[ApplicationRepository].project(personRepositoryInj => new PeopleService {
      val personRepository: ApplicationRepository = personRepositoryInj
    })
  }
}
