package services

import config.ApplicationConfig
import models.EmailStatus
import repository.PersonRepository
import zio.{Duration, Scope, ZIO, ZLayer}

import java.util.concurrent.TimeUnit

trait EmailService {

  private val LOCK_NAME = "main_sending_lock"

  def sendEmail(emailStatus: EmailStatus): ZIO[Any, Throwable, Unit] = {

    // try to send email
    // if email sending is done update success and errors to database
    ZIO.succeed(())
  }

  def drainingQueueAndSendMessagesWithRetry: ZIO[PersonRepository & RedisDatabase, Throwable, Long] = {
    for {
      personRepository <- ZIO.service[PersonRepository]
      redisDatabase <- ZIO.service[RedisDatabase]
      _ <- redisDatabase.acquireLock(LOCK_NAME, Duration.apply(5, TimeUnit.MINUTES).toMillis)
      emails <- personRepository.getAllEmailsPending
      results <- ZIO.collectAll(emails.map { email =>
        (for {
          _ <- personRepository.updateEmailStatus(email.id, "PENDING")
          _ <- sendEmail(email)
          _ <- personRepository.updateEmailStatus(email.id, "DONE")
        } yield {
          1
        })
          .catchAll { exception =>
            // log.error
            ZIO.succeed(0)
          }
      })
      totalSuccessCount <- ZIO.succeed(results.sum())
      // if the process fails it will not release the lock
      _ <- redisDatabase.releaseLock(LOCK_NAME)
    } yield {
      totalSuccessCount
    }
  }

}

object EmailService {
  val live: ZLayer[ApplicationConfig, Nothing, EmailService] = {
    ZLayer.service[ApplicationConfig].project(config => new EmailService {})
  }
}