package services

import zio.{ZIO, ZLayer}
import zio.redis.Redis


trait RedisDatabase {

  val redisDatabase:Redis

  def acquireLock(name: String, lockTimeout: Long): ZIO[Any, Throwable, Unit] = {
    for {
      result <- redisDatabase.setNx(name, "LOCK")
    } yield {
      if(result)
        ZIO.succeed(())
      else
        ZIO.fail(new Throwable("Failed to acquire lock on redis database"))
    }
  }

  def releaseLock(name: String): ZIO[Any, Throwable, Long] = {
    for {
      result <- redisDatabase.del(name)
    } yield result
  }
}

object RedisDatabase {
  val live: ZLayer[Redis, Nothing, RedisDatabase] = {
    ZLayer.service[Redis].project(redis =>
      new RedisDatabase {
        val redisDatabase: Redis = redis
      }
    )
  }
}


/*
    // Try to set value to the cache api
    // if result is nil throw exception
    // if result is OK then return unit
    // Redis save with NX and timeout

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