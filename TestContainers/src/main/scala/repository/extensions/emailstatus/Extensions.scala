package repository.extensions.emailstatus

import models.{EmailStatus, Person}
import org.bson.Document

import java.util.UUID

extension (input: EmailStatus)
  def toEmailStatusMongoObject: Document = {
    new Document()
      .append("personId", input.personId)
      .append("requestedAt", input.requestedAt)
      .append("status", input.status)
  }

extension (document: Document)
  def fromEmailStatusMongoObject: EmailStatus = {
    EmailStatus(
      personId = document.getString("personId"),
      requestedAt = document.getLong("requestedAt"),
      status = document.getString("status")
    )
  }
