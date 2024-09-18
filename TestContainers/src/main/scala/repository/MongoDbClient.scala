package repository

import com.mongodb.client.{MongoClient, MongoClients}
import config.ApplicationConfig
import zio.ZLayer
import zio.ZIO

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

/*

object MongoDbClient {

  val layer: ZLayer[A with B, Nothing, C] =
    ZLayer {
      for {
        a <- ZIO.service[A]
        b <- ZIO.service[B]
      } yield C(a, b)
    }

  val layer: ZLayer[ApplicationConfig, Nothing, MongoDbClient] = {
    ZLayer.succeed(ZIO.service[ApplicationConfig].map { appConfig =>
      new MongoDbClient(MongoClients.create(appConfig.databaseUrl))
    })
  }

}
*/
