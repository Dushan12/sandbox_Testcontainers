package actors

import config.ApplicationConfig
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.scaladsl.{ActorContext, Behaviors}
import repository.PersonRepository
import services.RabbitMqService
import zio.{Schedule, Unsafe, ZIO, ZLayer}
import java.time.temporal.ChronoUnit

/***
 * Why use queue when we can send them and reload them
 * Why not use database
 *
 * If you see this from a perspective where the service is hosted on distributed environment
 * even if the service is singleton in the application it has multiple services like this on every instance of the
 * cluster. So if you have five instances all of them will try to pull from database and send the message
 * and this causes issue if you want the system to send the mail only once.
 *
 * Queue have a deduplication mechanisms that if a message is seen on the exchange it will be ignored in the next occurance
 * Redis has mechanism where when you store the value you can specify NX=true which means only once instance will get to save
 * the value and then all others will get the cached value which is also an option.
 * In that case the app will try to store a value and if it succeeds then send the mail
 * if the app do not succeed means another instance already did and fail gracefully or get the result from Reddis
 * https://redis.io/docs/latest/develop/use/patterns/distributed-locks/


 */

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