package de.blu.database.config.redis;

public interface RedisConfig {
  boolean isEnabled();

  String getHost();

  int getPort();

  String getPassword();

  void setEnabled(boolean value);

  void setHost(String host);

  void setPort(int port);

  void setPassword(String password);

  void copyFrom(RedisConfig redisConfig);
}
