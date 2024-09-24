package integrations

class EmailSendingMechanismIntegration {

  // update lead in database
  // create email sending request in database transactional
  // it should have a mongodb container that will get the change
  // it should try to acquire lock on reddis
  // if lock is ackured then mails should be sending
  // For test purpose EmailSending can implement zio.Console and we can assert that the mails are being sent
  //

  // or redis container
}
