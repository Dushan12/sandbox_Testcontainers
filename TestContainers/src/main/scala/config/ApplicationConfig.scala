package config

import zio.ZEnvironment

//trait ApplicationConfig

case class ApplicationConfig(dbName: String, peopleCollectionName: String, databaseUrl: String)
// "mongodb://localhost:27017"

