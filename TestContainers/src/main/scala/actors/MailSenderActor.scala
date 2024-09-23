package actors

import org.apache.pekko.actor.typed.{ActorRef, Behavior}
import org.apache.pekko.actor.typed.scaladsl.Behaviors


object MailSenderActor {
  final case class SendPendingMails(whom: String)
  final case class CheckInconsistentSending(whom: String)

  def apply(): Behavior[SendPendingMails] = Behaviors.receive { (context, message) =>
    // Context start actor
    // spawn timer that executes every 10 seconds and calls a "Send" method
    // change the behaviour to the actor to listen to send
    // make sure future executions do not kill the actor or stop it from processing
    // the scheduler will ping every 30 seconds and load all messages pending for sending
    // the scheduler will ping every 10 minutes and check for inconsistent messages that will eventually get moved to FAILURE status
/*
    context.log.info("Hello {}!", message.whom)
    message.replyTo ! Greeted(message.whom, context.self)*/
    Behaviors.same
  }
}