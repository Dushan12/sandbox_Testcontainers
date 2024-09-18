package repository

import com.mongodb.client.{MongoClient, MongoClients}
import config.ApplicationConfig
import zio.ZLayer

trait MongoDbClient {
  def client: MongoClient
}

object MongoDbClient {

  val live: ZLayer[ApplicationConfig, Nothing, MongoDbClient] = {
    ZLayer.service[ApplicationConfig].project(config => new MongoDbClient {
      def client: MongoClient = MongoClients.create(config.databaseUrl)
    })
  }

}
