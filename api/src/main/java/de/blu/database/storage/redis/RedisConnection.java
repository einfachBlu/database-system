package de.blu.database.storage.redis;

import de.blu.database.config.redis.RedisConfig;
import de.blu.database.storage.KeyValueStorage;
import de.blu.database.storage.pubsub.PubSub;

public interface RedisConnection extends KeyValueStorage, PubSub {
  void init(RedisConfig redisConfig);
}
