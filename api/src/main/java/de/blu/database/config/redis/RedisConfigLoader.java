package de.blu.database.config.redis;

import java.io.File;

public interface RedisConfigLoader {
  void init(RedisConfig redisConfig, File configDirectory);

  void load();

  void save();
}
