package config

import zio.ZLayer

trait ApplicationConfig {
  val dbName: String
  val peopleCollectionName: String
  val emailStatusCollectionName: String
  val databaseUrl: String
}

object ApplicationConfig {

  val live: ZLayer[Any, Nothing, ApplicationConfig] =
    ZLayer.succeed(new ApplicationConfig {
      val dbName: String = "testContainers"
      val peopleCollectionName: String  = "people"
      val emailStatusCollectionName: String  = "emailStatus"
      val databaseUrl: String = "mongodb://localhost:27017"
    })

}
