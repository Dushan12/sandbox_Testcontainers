package repository

import com.mongodb.MongoClientSettings
import com.mongodb.client.{MongoClient, MongoClients}
import config.ApplicationConfig
import models.Person
import repository.extensions.person.{fromMongoObject, toMongoObject}
import zio.{ZIO, ZLayer}

import scala.collection.immutable

object PersonUal {

  def insertOne(person: Person): ZIO[ApplicationConfig & MongoDbClient, Throwable, Boolean] = {
    for {
      config <- ZIO.service[ApplicationConfig]
      client <- ZIO.service[MongoDbClient]
    } yield {
      println(client)
      val collection = client.client.getDatabase(config.dbName).getCollection(config.peopleCollectionName)
      collection.insertOne(person.toMongoObject).wasAcknowledged()
    }
  }

  def getAll: ZIO[ApplicationConfig & MongoDbClient, Throwable, immutable.List[Person]] = {
    for {
      config <- ZIO.service[ApplicationConfig]
      client <- ZIO.service[MongoDbClient]
    } yield {
      println(client)
      val collection = client.client.getDatabase(config.dbName).getCollection(config.peopleCollectionName)
      val records = collection.find().map { x => x.fromMongoObject }
      var outRecords = List.empty[Person]
      records.iterator().forEachRemaining { item =>
        outRecords = outRecords ++ List(item)
      }
      outRecords
    }
  }

}