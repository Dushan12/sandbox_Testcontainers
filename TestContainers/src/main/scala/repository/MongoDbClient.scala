package repository

import com.mongodb.client.{MongoClient, MongoClients, MongoCollection}
import config.ApplicationConfig
import org.bson.Document
import zio.ZLayer

trait MongoDbClient {
  val client: MongoClient
  def getPeopleCollection: MongoCollection[Document]
}

object MongoDbClient {

  val live: ZLayer[ApplicationConfig, Nothing, MongoDbClient] = {
    ZLayer.service[ApplicationConfig].project(config => new MongoDbClient {
      val client: MongoClient = MongoClients.create(config.databaseUrl)
      override def getPeopleCollection: MongoCollection[Document] = {
        client.getDatabase(config.dbName).getCollection(config.peopleCollectionName)
      }
    })
  }

}
