package services

import com.mongodb.client.{MongoClient, MongoClients}
import models.Person
import org.bson.Document
import zio.{ZIO, ZLayer}

object PeopleService {



  def sp1(person: Person): ZIO[MongoClient, Throwable, Boolean] = {
    val vv = (for {
      client <- ZIO.service[MongoClient]
      res <- ZIO.attempt(
        client.getDatabase("db").getCollection("people").insertOne(new Document()
          .append("firstName", person.firstName)
          .append("lastName", person.lastName)
          .append("email", person.email)
        )
      )
    } yield {
      res.wasAcknowledged()
    })

    vv
  }

  def getClient(): ZIO[Any, Throwable, MongoClient] = {
    ZIO.attempt(MongoClients.create("mongodb://localhost:27017"))
  }


  def savePerson(person: Person): ZIO[Any, Throwable, Boolean] = {
    sp1(person: Person).provide(ZLayer.apply(getClient()))
  }

  def getPeople(): List[Person] = {
    throw new NotImplementedError()
  }

}
