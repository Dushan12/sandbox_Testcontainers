package repository.extensions.person

import models.Person
import org.bson.Document

extension (input: Person)
  def toMongoObject: Document = {
    new Document()
      .append("firstName", input.firstName)
      .append("lastName", input.lastName)
      .append("email", input.email)
  }
