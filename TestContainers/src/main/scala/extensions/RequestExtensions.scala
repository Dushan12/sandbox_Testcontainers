package extensions

import zio.ZIO
import zio.http.Request
import zio.json.*

extension (request: Request)
  def asObject[T](implicit decoder: zio.json.JsonDecoder[T]): ZIO[Any, String, T] = {
    request.body.asString.mapError(_.getMessage).flatMap { bodyStr =>
      ZIO.fromEither(bodyStr.fromJson[T])
    }
  }
