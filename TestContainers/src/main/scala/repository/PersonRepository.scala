package repository

import com.mongodb.client.result.UpdateResult
import com.mongodb.client.{ClientSession, MongoClient, MongoClients, MongoCollection}
import config.ApplicationConfig
import models.{EmailStatus, Person}
import org.bson.{BsonDocument, BsonString, Document}
import repository.extensions.emailstatus.*
import repository.extensions.person.{fromMongoObject, toMongoObject}
import zio.{ZIO, ZLayer}

import scala.collection.immutable

trait PersonRepository {

  val client: MongoClient
  val config: ApplicationConfig

  def getPeopleCollection: MongoCollection[Document] = {
      client.getDatabase(config.dbName).getCollection(config.peopleCollectionName)
  }

  def getEmailStatusCollection: MongoCollection[Document] = {
      client.getDatabase(config.dbName).getCollection(config.emailStatusCollectionName)
  }

  def insertOne(person: Person): ZIO[Any, Throwable, Boolean] = {
    ZIO.succeed(getPeopleCollection.insertOne(person.toMongoObject).wasAcknowledged())
  }

  def getAll: ZIO[Any, Throwable, immutable.List[Person]] = {
    ZIO.attempt {
      val records = getPeopleCollection.find().map { x => x.fromMongoObject }
      var outRecords = List.empty[Person]
      records.iterator().forEachRemaining { item =>
        outRecords = outRecords ++ List(item)
      }
      outRecords
    }
  }


  def getAllEmailsPending: ZIO[Any, Throwable, List[EmailStatus]] = {
       ZIO.attempt {
         val records = getPeopleCollection.find(BsonDocument("status", BsonString("PENDING"))).map { x => x.fromEmailStatusMongoObject }
         var outRecords = List.empty[EmailStatus]
         records.iterator().forEachRemaining { item =>
           outRecords = outRecords ++ List(item)
         }
         outRecords
       }
  }

  def updateEmailStatus(id: String, newStatus: String): ZIO[Any, Nothing, Boolean] = {
      for {
        collection <- ZIO.succeed(getEmailStatusCollection)
      } yield {
        collection.updateOne(BsonDocument("id", BsonString(id)), BsonDocument("$set", BsonDocument("status", BsonString(newStatus)))).wasAcknowledged()
      }
  }

  def updatePersonAndStoreEmailRequest(id: String, firstName: String): ZIO[Any, Throwable, Unit] = {
    ZIO.attempt {
      for {
        session <- ZIO.succeed(client.startSession())
        collection <- ZIO.succeed(getPeopleCollection)
        _ <- ZIO.succeed(collection.updateOne(session, BsonDocument("id", BsonString(id)), BsonDocument("$set", BsonDocument("firstName", BsonString(firstName)))))
        _ <- ZIO.succeed(collection.insertOne(EmailStatus(id = "1", personId = id, requestedAt = System.currentTimeMillis(), status = "PENDING").toEmailStatusMongoObject))
      } yield {
        session.commitTransaction()
      }
    }
  }
}

object PersonRepository {
  val live: ZLayer[ApplicationConfig, Nothing, PersonRepository] = {
    ZLayer.service[ApplicationConfig].project(config => new PersonRepository {
      val config: ApplicationConfig = config
      val client: MongoClient = MongoClients.create(config.databaseUrl)
    })
  }
}