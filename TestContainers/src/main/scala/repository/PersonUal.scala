package repository

import com.mongodb.client.MongoClient
import config.ApplicationConfig
import models.Person
import repository.extensions.person.{fromMongoObject, toMongoObject}
import zio.ZIO

import java.util
import scala.collection.immutable
import scala.io.Source

object PersonUal {

  def insertOne(person: Person): ZIO[MongoClient & ApplicationConfig, Throwable, Boolean] = {
    for {
      client <- ZIO.service[MongoClient]
      config <- ZIO.service[ApplicationConfig]
    } yield {
      val collection = client.getDatabase(config.dbName).getCollection(config.peopleCollectionName)
      collection.insertOne(person.toMongoObject).wasAcknowledged()
    }
  }

  def getAll: ZIO[MongoClient & ApplicationConfig, Throwable, immutable.List[Person]] = {
    for {
      client <- ZIO.service[MongoClient]
      config <- ZIO.service[ApplicationConfig]
    } yield {
      val collection = client.getDatabase(config.dbName).getCollection(config.peopleCollectionName)
      val records = collection.find().map { x => x.fromMongoObject }
      List(records.first())
    }
  }

}