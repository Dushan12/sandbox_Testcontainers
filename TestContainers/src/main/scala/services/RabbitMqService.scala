package services

import config.ApplicationConfig
import models.EmailStatus
import zio.{ZIO, ZLayer}


trait RabbitMqService {

  def sendMessageToQueue(emailStatus: EmailStatus): ZIO[Any, Throwable, Unit]

}

object RabbitMqService {
  val live: ZLayer[ApplicationConfig, Nothing, RabbitMqService] = {
    ZLayer.service[ApplicationConfig].project(config => new RabbitMqService {

      override def sendMessageToQueue(emailStatus: EmailStatus): ZIO[Any, Throwable, Unit] = {
        ZIO.succeed(())
      }

    })
  }
}