package services

import config.ApplicationConfig
import zio.ZIO
import zio.ZLayer

trait RedisDatabase {
  def acquireLock(name: String, lockTimeout: Long): ZIO[Any, Throwable, Unit] = {
    // Try to set value to the cache api
    // if result is nil throw exception
    // if result is OK then return unit
    // Redis save with NX and timeout
    /*
127.0.0.1:6379>     SET resource_name1 my_random_value NX PX 3000
OK
127.0.0.1:6379>     SET resource_name1 my_random_value NX PX 3000
(nil)
127.0.0.1:6379>     SET resource_name1 my_random_value NX PX 30000
OK
127.0.0.1:6379>     SET resource_name1 my_random_value NX PX 30000
(nil)
127.0.0.1:6379> DEL resource_name1
(integer) 1
127.0.0.1:6379>     SET resource_name1 my_random_value NX PX 30000
OK
127.0.0.1:6379>     SET resource_name1 my_random_value NX PX 300000
(nil)
127.0.0.1:6379> DEL resource_name1
(integer) 1
127.0.0.1:6379>     SET resource_name1 my_random_value NX PX 300000
OK

     */
    ZIO.succeed(())
  }

  def releaseLock(name: String): ZIO[Any, Throwable, Unit] = {
    ZIO.succeed(())
  }
}

object RedisDatabase {
  val live: ZLayer[ApplicationConfig, Nothing, RedisDatabase] = {
    ZLayer.service[Any].project(config => new RedisDatabase {})
  }
}