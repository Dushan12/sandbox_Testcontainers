package services

import config.ApplicationConfig
import zio.{ZIO, ZLayer}

trait EmailService {

  def sendEmail: ZIO[Any, Throwable, Unit]

}

object EmailService {
  val live: ZLayer[ApplicationConfig, Nothing, EmailService] = {
    ZLayer.service[ApplicationConfig].project(config => new EmailService {

      override def sendEmail: ZIO[Any, Throwable, Unit] = {
        ZIO.succeed(())
      }

    })
  }
}