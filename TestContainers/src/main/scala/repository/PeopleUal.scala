package repository

import models.Person
import org.bson.codecs.configuration.CodecProvider
import zio.{Task, ZIO, ZLayer}

case class PersonDao(firstName: String, lastName: String, email: String)
