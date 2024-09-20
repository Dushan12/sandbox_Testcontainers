package config

import zio.{ZIO, ZLayer}

trait ApplicationConfig {
  def dbName: String
  def peopleCollectionName: String
  def databaseUrl: String
}

object ApplicationConfig {

  val live: ZLayer[Any, Nothing, ApplicationConfig] =
    ZLayer.succeed(new ApplicationConfig {
      def dbName: String = "testContainers"
      def peopleCollectionName: String  = "people"
      def databaseUrl: String = "mongodb://localhost:27017"
    })

}
