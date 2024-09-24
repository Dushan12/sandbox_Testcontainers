package actors

import config.ApplicationConfig
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.scaladsl.{ActorContext, Behaviors}
import repository.PersonRepository
import services.RabbitMqService
import zio.{Schedule, Unsafe, ZIO, ZLayer}

import java.time.temporal.ChronoUnit


object MailQueueActor {
  final case class QueuePendingMails()

  def apply(): Behavior[QueuePendingMails] =  Behaviors.setup { context =>

    ZIO.succeed(context.self.tell(QueuePendingMails())).repeat(Schedule.fixed(zio.Duration.apply(10, ChronoUnit.MINUTES)))

    Behaviors.receive[QueuePendingMails] { (context, message) =>
      Unsafe.unsafe { implicit unsafe =>
        zio.Runtime.default.unsafe.run {
          (for {
            personRepository <- ZIO.service[PersonRepository]
            rabbitMqService <- ZIO.service[RabbitMqService]
            emails <- personRepository.getAllEmailsPending
            _ <- ZIO.collectAll(emails.map { email =>
              rabbitMqService.sendMessageToQueue(email)
            })
          } yield {
            Behaviors.same[QueuePendingMails]
          }).provide(
            ApplicationConfig.live >>> RabbitMqService.live,
            ApplicationConfig.live >>> PersonRepository.live
          )
        }.getOrElse { _ =>
          // log failure
          Behaviors.same[QueuePendingMails]
        }
      }
    }
  }
}