package integrations

class EmailSendingMechanismIntegration {

  // update lead in database
  // it should have a mongodb container that will get the change
  // it should have a rabbitmq container that will contain the queue messages
  // it should pull messages from rabbit container and execute sending message
  // it should call a send message service (Maybe for simplicity it will be an API service that does not have container and just validate if it gets called)
}
