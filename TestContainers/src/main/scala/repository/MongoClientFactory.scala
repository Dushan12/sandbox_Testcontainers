package repository

import com.mongodb.client.{MongoClient, MongoClients}
import config.ApplicationConfig
import zio.{Ref, UIO, ZIO}

trait MongoClientFactory {

  var mongoClient: Ref[Option[MongoClient]]

  def getClient: ZIO[ApplicationConfig, Nothing, UIO[Any]] = {
    for {
      config <- ZIO.service[ApplicationConfig]
      client <- mongoClient.get
    } yield {
      if(client.isEmpty) {
        mongoClient.set(Some(MongoClients.create(config.databaseUrl)))
      }
      mongoClient.get
    }
  }


}
