package services

import config.ApplicationConfig
import models.EmailStatus
import repository.PersonRepository
import zio.{Duration, Scope, ZIO, ZLayer}

import java.util.concurrent.TimeUnit

trait EmailService {

  private val LOCK_NAME = "main_sending_lock"

  private def sendEmail(emailStatus: EmailStatus): ZIO[Any, Throwable, Unit] = {

    // try to send email
    // if email sending is done update success and errors to database
    ZIO.succeed(())
  }

  def drainingQueueAndSendMessagesWithRetry: ZIO[PersonRepository & RedisDatabase, Throwable, Unit] = {
    for {
      personRepository <- ZIO.service[PersonRepository]
      redisDatabase <- ZIO.service[RedisDatabase]
      _ <- redisDatabase.acquireLock(LOCK_NAME, Duration.apply(5, TimeUnit.MINUTES).toMillis)
      emails <- personRepository.getAllEmailsPending
      _ <- ZIO.collectAll(emails.map { email =>
        sendEmail(email)
      }).catchAll { _ =>
        // log failed while sending errors
        // catch errors here to make sure we do not block the lock release
        ZIO.succeed(())
      }
      // if the process fails it will not release the lock
      _ <- redisDatabase.releaseLock(LOCK_NAME)
    } yield {
      ()
    }
  }

}

object EmailService {
  val live: ZLayer[ApplicationConfig, Nothing, EmailService] = {
    ZLayer.service[ApplicationConfig].project(config => new EmailService {})
  }
}