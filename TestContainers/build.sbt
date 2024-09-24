import scala.collection.Seq

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.5.0"

val zioVersion = "2.1.9"
val zioJsonVersion = "0.7.3"
val zioHttpVersion = "3.0.0"
val zioMongoDbVersion = "5.1.4"
val zioTestContainersVersion = "0.5.0"
val zioAmqpVersion = "1.0.0"
val zioPekkoVersion = "1.1.1"

val dependencies = Seq(
  "dev.zio"                 %% "zio"                    % zioVersion,
  "dev.zio"                 %% "zio-streams"            % zioVersion,
  "dev.zio"                 %% "zio-http"               % zioHttpVersion,
  "dev.zio"                 %% "zio-json"               % zioJsonVersion,
  "org.mongodb"             % "mongodb-driver-sync"     % zioMongoDbVersion,
  "org.apache.pekko"        %% "pekko-actor-typed"      % zioPekkoVersion,
  "dev.zio"                 %% "zio-amqp"               % zioAmqpVersion,
  "dev.zio"                 %% "zio-test"               % zioVersion % Test,
  "dev.zio"                 %% "zio-test-sbt"           % zioVersion % Test,
  "dev.zio"                 %% "zio-test-magnolia"      % zioVersion % Test,
  "com.github.sideeffffect" %% "zio-testcontainers"     % zioTestContainersVersion % Test
)

libraryDependencies := dependencies

lazy val root = (project in file("."))
  .settings(
    name := "TestContainers"
  )
