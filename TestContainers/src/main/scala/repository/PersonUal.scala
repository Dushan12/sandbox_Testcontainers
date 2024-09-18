package repository

import com.mongodb.MongoClientSettings
import com.mongodb.client.{MongoClient, MongoClients}
import config.ApplicationConfig
import models.Person
import repository.extensions.person.{fromMongoObject, toMongoObject}
import zio.{ZIO, ZLayer}

import scala.collection.immutable

object PersonUal {

  def insertOne(person: Person): ZIO[ApplicationConfig, Throwable, Boolean] = {
    ZIO.service[ApplicationConfig].flatMap { config =>
      (for {
        client <- ZIO.service[MongoClient]
      } yield {
        val collection = client.getDatabase(config.dbName).getCollection(config.peopleCollectionName)
        collection.insertOne(person.toMongoObject).wasAcknowledged()
      }).provide(
        ZLayer.apply(ZIO.attempt(MongoClients.create(config.databaseUrl)))
      )
    }
  }

  def getAll: ZIO[ApplicationConfig, Throwable, immutable.List[Person]] = {
    ZIO.service[ApplicationConfig].flatMap { config =>
      (for {
        client <- ZIO.service[MongoClient]
      } yield {
        val collection = client.getDatabase(config.dbName).getCollection(config.peopleCollectionName)
        val records = collection.find().map { x => x.fromMongoObject }
        var outRecords = List.empty[Person]
        records.iterator().forEachRemaining { item =>
          outRecords = outRecords ++ List(item)
        }
        outRecords
      }).provide(
        ZLayer.apply(ZIO.attempt(MongoClients.create(config.databaseUrl)))
      )
    }
  }

}