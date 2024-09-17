package repository

import com.mongodb.client.MongoClient
import models.Person
import repository.extensions.person.toMongoObject
import zio.ZIO

object PersonUal {

  def insertOne(person: Person): ZIO[MongoClient, Throwable, Boolean] = {
    ZIO.service[MongoClient].map { client =>
      val collection = client.getDatabase("testContainers").getCollection("people")
      collection.insertOne(person.toMongoObject).wasAcknowledged()
    }
  }

}