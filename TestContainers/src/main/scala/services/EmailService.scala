package services

import config.ApplicationConfig
import models.EmailStatus
import zio.Console.printLine
import zio.amqp.model.{ConsumerTag, DeliveryTag, QueueName}
import zio.amqp.{Amqp, Channel}
import zio.{Scope, ZIO, ZLayer}

import java.net.URI

trait EmailService {

  def sendEmail(emailStatus: EmailStatus): ZIO[Any, Throwable, Unit]

  def drainingQueueAndSendMessagesWithRetry: ZIO[Scope & ApplicationConfig, Throwable, Unit] = {
    val channel: ZIO[Scope, Throwable, Channel] = for {
      connection <- Amqp.connect(URI.create("amqp://my_amqp_server_uri"))
      channel <- Amqp.createChannel(connection)
    } yield channel

    channel.flatMap { channel =>
      channel
        .consume(queue = QueueName("queueName"), consumerTag = ConsumerTag("test"))
        .mapZIO { record =>
          val deliveryTag = record.getEnvelope.getDeliveryTag
          sendEmail(null)
            .flatMap {_ =>
            channel.ack(DeliveryTag(deliveryTag))
          }

        }
        .runDrain
    }
  }

}

object EmailService {
  val live: ZLayer[ApplicationConfig, Nothing, EmailService] = {
    ZLayer.service[ApplicationConfig].project(config => new EmailService {

      override def sendEmail(emailStatus: EmailStatus): ZIO[Any, Throwable, Unit] = {
        ZIO.succeed(())
      }

    })
  }
}