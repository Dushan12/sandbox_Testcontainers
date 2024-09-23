package services

import models.Person
import repository.PersonUal
import zio.{Unsafe, ZIO}

object PeopleService {

  def savePerson(person: Person): ZIO[PersonUal, Throwable, Boolean] = {
    for {
      personUal <- ZIO.service[PersonUal]
      insertOneResult <- personUal.insertOne(person)
    } yield {
      insertOneResult
    }
  }

  def getPeople: ZIO[PersonUal, Throwable, List[Person]] = {
    for {
      personUal <- ZIO.service[PersonUal]
      results <- personUal.getAll
    } yield {
      results
    }
  }

  /*

   */
  def updatePerson(id: String, firstName: String): ZIO[PersonUal & EmailService, Nothing, Unit] = {

    // database update persons first name
      // if database update fails then Throw error in ZIO failure channel
    // email service send email that the name was changed
      // if email sending fails then throw throwable in ZIO channel and revert database update

   // @@ If we start transaction and then while transaction is opened do API call. If API call succeeds we do a commit
   // @@ we are still in risk that the transaction commit will fail and the database will not update

    // @@ If we do API call and only if it succeeds do the database update we are in a state where the mail is sent but the database
    // @@ update failed and if user clicks several times it might send several mails for the update

    // @@ If we update the database that the mail is sent and then call the mail service API
    // @@ the database update succeeds and email fails, the database if left in inconsistent state with the update, the function returns error
    // @@ and the mail is not sent

    // @@ possible solution
    // update database entry and create database object to send message request
    // the update will need to be idempotent. the send message request should be identifiable. ex. Message user for object update
    // if it fails throw error
    // spawn daemon that will see all messages that are pending for sending
    // update database send message object with status "SENDING"
    // try to send
    // if it fails then write to database that the sending failed
    // if message sends but database update fails then the message will not be sent several times.
    // if it completes then write to database that it is complete

    // Database inconsistency will be all objects that are in status sending for longer than x.
    // Meaning that they were marked as sending but never updated as sent



    for {
      personUal <- ZIO.service[PersonUal]
      emailService <- ZIO.service[EmailService]
    } yield {

      // @@ we have person persistence and email persistence
      // @@ we need to store them together transactional
      // @@ if we put the code in person ual then the transaction logic is hidden but one UAL object is reponsible for two collections
      // @@ if we pull the transaction in the service the Mongo logic is visible in the service itself and does not encapsulate well
      // @@ but i guess the application layer should contain this kind of specifics (it is not a domain model)
       // ===========================
       /*
       Funds Transfer App Service:
         1.Digests input (e.g. XML request),
         2.sends message to domain service for fulfillment,
         3.listens for confirmation,
         4.decides to send notification using infrastructure service
        */
       // ===========================
      // Open transaction and update the person object
      // in the same transaction create the message send request object
      // Create a daemon that will spawn every 10 seconds and get all "Send Message Request" objects with status "PENDING"
      // Update the status from "PENDING" to "SENDING" and update the created at to latest timestamp
      // THE PROBLEM HERE IS THAT THE DAEMON THAT PULLS THE DATA MUST BE CLUSTER SINGLETON
      // THE SINGLETON CAN RELY ON RABBIT MQ DEDUPLICATION PLUGIN
      // OR THE WORKERS CAN CONSULT REDIS CACHE THAT WILL ONLY UPDATE ONCE BECAUSE OF NX = TRUE

      // @@ create database record with lock in redis NX = true
      // @@ if saved try to send mail with retry
      // @@ if anybody calls the method meanwhile return result from cache
      // @@ if anybody calls the method when done return result from cache
      // @@ if email sending fails write to database
      // @@ if email sending succeeds write to database

      // Try to send and if it succeeds update the status to "COMPLETED"
      // if it fails do nothing and log error and try to update the status to "FAILED"
      // create another daemon that will pull all objects with status "SENDING" and timestamp greater than 10 minutes and update the status to "FAILED"

      // DISCUSSION
      // if update object fails it will return error
      // if daemon1 fails no mail will be sent from the system
      // if daemon2 fails no record that is stuck in status "SENDING" will be moved to failed
      // if connection to db fails it will most likely fail to move the status from "PENDING" to "SENDING"
      // if it gets stuck in sending it has a mechanism to get moved to "FAILED" automatically
      // if it succeeds sending but fails to update status it will not be retried again
      // the mail sending duration will be processed in separate async thread and will not affect the duration of the request
      // The downside is that it does not have a retry method
      // The retry method will not be in the QUEUE itself but maybe in an app like "Retries" that will try several times with backoff algorithm


      ZIO.succeed(())


      // @POSSIBLE SOLUTION
      // UPDATE REDIS CACHE AND RETURN UNIT
      // TRY TO CREATE OR UPDATE DATABASE AND SEND MAIL SEVERAL TIMES.
      // ISSUE WITH THIS WOULD BE THAT IF APP CRASHES IT WILL NOT PROCEED
    }
  }

}
