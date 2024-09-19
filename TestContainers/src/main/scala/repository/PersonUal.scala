package repository

import models.Person
import repository.extensions.person.{fromMongoObject, toMongoObject}
import zio.ZIO

import scala.collection.immutable

object PersonUal {

  def insertOne(person: Person): ZIO[MongoDbClient, Throwable, Boolean] = {
    for {
      client <- ZIO.service[MongoDbClient]
    } yield {
      println(client.client)
      client.getPeopleCollection.insertOne(person.toMongoObject).wasAcknowledged()
    }
  }

  def getAll: ZIO[MongoDbClient, Throwable, immutable.List[Person]] = {
    for {
      client <- ZIO.service[MongoDbClient]
    } yield {
      println(client.client)
      val records = client.getPeopleCollection.find().map { x => x.fromMongoObject }
      var outRecords = List.empty[Person]
      records.iterator().forEachRemaining { item =>
        outRecords = outRecords ++ List(item)
      }
      outRecords
    }
  }

}