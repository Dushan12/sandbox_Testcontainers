package repository.extensions.emailstatus

import models.EmailStatus
import org.bson.Document

extension (input: EmailStatus)
  def toEmailStatusMongoObject: Document = {
    new Document()
      .append("id", input.id)
      .append("personId", input.personId)
      .append("requestedAt", input.requestedAt)
      .append("status", input.status)
  }

extension (document: Document)
  def fromEmailStatusMongoObject: EmailStatus = {
    EmailStatus(
      id = document.getString("id"),
      personId = document.getString("personId"),
      requestedAt = document.getLong("requestedAt"),
      status = document.getString("status")
    )
  }
