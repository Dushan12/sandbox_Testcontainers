package config

import zio.{ZEnvironment, ZLayer}

//trait ApplicationConfig

//case class ApplicationConfig(dbName: String, peopleCollectionName: String, databaseUrl: String)
// "mongodb://localhost:27017"



trait ApplicationConfig {
  def dbName: String
  def peopleCollectionName: String
  def databaseUrl: String
}

object ApplicationConfig {

  val live: ZLayer[Any, Nothing, ApplicationConfig] =
    ZLayer.succeed(new ApplicationConfig {
      def dbName = "testContainers"
      def peopleCollectionName  = "people"
      def databaseUrl = "mongodb://localhost:27017"
    })
}
