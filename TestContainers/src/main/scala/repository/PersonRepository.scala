package repository

import com.mongodb.ClientSessionOptions
import com.mongodb.client.{ClientSession, MongoClient, MongoClients, MongoCollection}
import config.ApplicationConfig
import models.{EmailStatus, Person}
import org.bson.{BsonDocument, BsonString, Document}
import repository.extensions.person.{fromMongoObject, toMongoObject}
import repository.extensions.emailstatus._
import zio.{ZIO, ZLayer}

import scala.collection.immutable

// Inject a TransactionManager ???
// Inject a client ???
// If we do not want a transactional dependency then the email sending for person update should be part of the Person object
// It needs to be filtered out when getting the person tho.
// This way there is no transactions (Just update name, add to set a mail sending request)
trait PersonRepository {

  val client: MongoClient

  def getPeopleCollection: MongoCollection[Document]
  def getEmailStatusCollection: MongoCollection[Document]

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



  def getAllEmailsPending: ZIO[Any, Throwable, immutable.List[EmailStatus]] = {
    ZIO.attempt {
      val records = getEmailStatusCollection.find().map { x => x.fromEmailStatusMongoObject }
      var outRecords = List.empty[EmailStatus]
      records.iterator().forEachRemaining { item =>
        outRecords = outRecords ++ List(item)
      }
      outRecords
    }
  }

  def updatePersonAndStoreEmailRequest(id: String, firstName: String): ZIO[Any, Throwable, Unit] = {
    val mongoSession = client.startSession()
    val result = getPeopleCollection.updateOne(mongoSession, BsonDocument("id", BsonString(id)), BsonDocument("$set", BsonDocument("firstName", BsonString(firstName))))
    val resultCreate = getEmailStatusCollection.insertOne(EmailStatus(personId = id, requestedAt = System.currentTimeMillis(), status = "PENDING").toEmailStatusMongoObject)
    ZIO.attempt(mongoSession.commitTransaction())
  }

}

object PersonRepository {
  val live: ZLayer[ApplicationConfig, Nothing, PersonRepository] = {
    ZLayer.service[ApplicationConfig].project(config => new PersonRepository {

      val client: MongoClient = MongoClients.create(config.databaseUrl)

      override def getPeopleCollection: MongoCollection[Document] = {
        client.getDatabase(config.dbName).getCollection(config.peopleCollectionName)
      }
      override def getEmailStatusCollection: MongoCollection[Document] = {
        client.getDatabase(config.dbName).getCollection(config.emailStatusCollectionName)
      }

    })
  }
}