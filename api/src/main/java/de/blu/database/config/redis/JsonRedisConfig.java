package de.blu.database.config.redis;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class JsonRedisConfig implements RedisConfig {

  private boolean enabled = false;
  private String host = "127.0.0.1";
  private int port = 6379;
  private String password = "";

  @Override
  public void copyFrom(RedisConfig redisConfig) {
    this.enabled = redisConfig.isEnabled();
    this.host = redisConfig.getHost();
    this.port = redisConfig.getPort();
    this.password = redisConfig.getPassword();
  }
}
