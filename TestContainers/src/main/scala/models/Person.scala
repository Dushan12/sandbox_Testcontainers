package models

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

case class Person(id: String, firstName: String, lastName: String, email: String)

object Person {
  implicit val decoder: JsonDecoder[Person] = DeriveJsonDecoder.gen[Person]
  implicit val encoder: JsonEncoder[Person] = DeriveJsonEncoder.gen[Person]
}

case class EmailStatus(id: String, personId: String, requestedAt: Long, status: String)

object EmailStatus {
  implicit val decoder: JsonDecoder[EmailStatus] = DeriveJsonDecoder.gen[EmailStatus]
  implicit val encoder: JsonEncoder[EmailStatus] = DeriveJsonEncoder.gen[EmailStatus]
}