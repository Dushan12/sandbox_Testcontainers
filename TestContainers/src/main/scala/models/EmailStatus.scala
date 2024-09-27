package models

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

case class EmailStatus(id: String, personId: String, requestedAt: Long, status: String)

object EmailStatus {
  implicit val decoder: JsonDecoder[EmailStatus] = DeriveJsonDecoder.gen[EmailStatus]
  implicit val encoder: JsonEncoder[EmailStatus] = DeriveJsonEncoder.gen[EmailStatus]
}
