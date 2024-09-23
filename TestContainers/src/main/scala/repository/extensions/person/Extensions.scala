package repository.extensions.person

import models.Person
import org.bson.Document

import java.util.UUID

extension (input: Person)
  def toMongoObject: Document = {
    new Document()
      .append("firstName", input.firstName)
      .append("lastName", input.lastName)
      .append("email", input.email)
  }

extension (document: Document)
  def fromMongoObject: Person = {
    Person(
      id = UUID.randomUUID().toString,
      firstName = document.getString("firstName"),
      lastName = document.getString("lastName"),
      email = document.getString("email")
    )
  }
