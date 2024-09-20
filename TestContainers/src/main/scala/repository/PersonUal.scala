package repository

import com.mongodb.client.{MongoClient, MongoClients, MongoCollection}
import config.ApplicationConfig
import models.Person
import org.bson.Document
import repository.extensions.person.{fromMongoObject, toMongoObject}
import zio.{ZIO, ZLayer}

import scala.collection.immutable

trait PersonUal {

  val client: MongoClient

  def getPeopleCollection: MongoCollection[Document]

  def insertOne(person: Person): ZIO[Any, Throwable, Boolean] = {
      ZIO.succeed(getPeopleCollection.insertOne(person.toMongoObject).wasAcknowledged())
  }

  def getAll: ZIO[Any, Throwable, immutable.List[Person]] = {
      val records = getPeopleCollection.find().map { x => x.fromMongoObject }
      var outRecords = List.empty[Person]
      records.iterator().forEachRemaining { item =>
        outRecords = outRecords ++ List(item)
      }
      ZIO.succeed(outRecords)
  }

}

object PersonUal {
  val live: ZLayer[ApplicationConfig, Nothing, PersonUal] = {
    ZLayer.service[ApplicationConfig].project(config => new PersonUal {

      val client: MongoClient = MongoClients.create(config.databaseUrl)

      override def getPeopleCollection: MongoCollection[Document] = {
        client.getDatabase(config.dbName).getCollection(config.peopleCollectionName)
      }

    })
  }
}