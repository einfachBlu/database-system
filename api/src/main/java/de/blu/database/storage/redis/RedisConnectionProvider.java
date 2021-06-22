package de.blu.database.storage.redis;

import de.blu.database.config.redis.RedisConfig;
import de.blu.database.storage.pubsub.listener.PubSubListener;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.pubsub.RedisPubSubListener;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import lombok.Getter;

import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Singleton
@Getter
public final class RedisConnectionProvider implements RedisConnection {

  private static final int REDIS_EXPIRE_DEFAULT = (int) TimeUnit.DAYS.toSeconds(7);

  public RedisClient client;
  public StatefulRedisConnection<String, String> connectionCache;
  public StatefulRedisPubSubConnection<String, String> connectionPubsubListener;
  public StatefulRedisPubSubConnection<String, String> connectionPubsubPublish;
  public RedisCommands<String, String> redisCommandsCache;
  public RedisCommands<String, String> redisCommandsPubsubListener;
  public RedisCommands<String, String> redisCommandsPubsubPublish;

  private ExecutorService executorService = Executors.newCachedThreadPool();

  @Override
  public void init(RedisConfig redisConfig) {
    RedisURI.Builder builder =
        RedisURI.builder().withHost(redisConfig.getHost()).withPort(redisConfig.getPort());

    if (!redisConfig.getPassword().isEmpty()) {
      builder.withPassword(redisConfig.getPassword());
    }

    this.client = RedisClient.create(builder.build());

    this.client.setOptions(
        ClientOptions.builder()
            .autoReconnect(true)
            .disconnectedBehavior(ClientOptions.DisconnectedBehavior.ACCEPT_COMMANDS)
            .cancelCommandsOnReconnectFailure(false)
            .build());
  }

  @Override
  public void connect() {
    try {
      this.connectionCache = this.getClient().connect();
      this.redisCommandsCache = this.getConnectionCache().sync();
      this.connectionPubsubListener = this.getClient().connectPubSub();
      this.connectionPubsubPublish = this.getClient().connectPubSub();
      this.redisCommandsPubsubListener = this.getConnectionPubsubListener().sync();
      this.redisCommandsPubsubPublish = this.getConnectionPubsubPublish().sync();
    } catch (Exception e) {
      e.printStackTrace();
      this.client = null;
      this.connectionCache = null;
      this.redisCommandsCache = null;
      this.connectionPubsubListener = null;
      this.connectionPubsubPublish = null;
      this.redisCommandsPubsubListener = null;
      this.redisCommandsPubsubPublish = null;
    }
  }

  @Override
  public void disconnect() {
    if (this.getConnectionCache() != null) {
      this.getConnectionCache().close();
    }

    if (this.getClient() != null) {
      this.getClient().shutdown();
    }

    this.connectionCache = null;
    this.client = null;

    this.connectionPubsubListener = null;
    this.connectionPubsubPublish = null;

    this.redisCommandsCache = null;
    this.redisCommandsPubsubListener = null;
    this.redisCommandsPubsubPublish = null;
  }

  @Override
  public boolean isConnected() {
    return this.getClient() != null;
  }

  @Override
  public void set(String key, String value) {
    this.set(key, value, REDIS_EXPIRE_DEFAULT);
  }

  @Override
  public void set(String key, String value, int expireSeconds) {
    if (!this.isConnected()) {
      new Exception("Redis is not connected!").printStackTrace();
      return;
    }

    this.getRedisCommandsCache().set(key, value);
    this.getRedisCommandsCache().expire(key, expireSeconds);
  }

  @Override
  public void remove(String key) {
    if (!this.isConnected()) {
      new Exception("Redis is not connected!").printStackTrace();
      return;
    }

    this.getRedisCommandsCache().del(key);
  }

  @Override
  public Collection<String> getKeys(String key) {
    return this.getKeys(key, false);
  }

  @Override
  public Collection<String> getKeys(String key, boolean recursive) {
    Collection<String> keys = new HashSet<>();
    if (!this.isConnected()) {
      new Exception("Redis is not connected!").printStackTrace();
      return keys;
    }

    List<String> cachedKeys;
    if (key.equalsIgnoreCase("")) {
      cachedKeys = this.getRedisCommandsCache().keys("*");
    } else {
      cachedKeys = this.getRedisCommandsCache().keys(key + ".*");
    }

    for (String cachedKey : cachedKeys) {
      if (recursive) {
        keys.add(cachedKey);
        continue;
      }

      cachedKey = cachedKey.substring(key.length() + (!key.equalsIgnoreCase("") ? 1 : 0));
      cachedKey = cachedKey.split("\\.")[0];

      keys.add(cachedKey);
    }

    return keys;
  }

  @Override
  public String get(String key) {
    if (!this.isConnected()) {
      new Exception("Redis is not connected!").printStackTrace();
      return null;
    }

    try {
      return this.getRedisCommandsCache().get(key);
    } catch (Exception e) {
      // key doesnt exist or is not from type string
    }

    return "";
  }

  @Override
  public Map<String, String> getAll() {
    if (!this.isConnected()) {
      new Exception("Redis is not connected!").printStackTrace();
      return null;
    }

    List<String> keys = this.getRedisCommandsCache().keys("*");
    Map<String, String> data = new LinkedHashMap<>();

    for (String key : keys) {
      data.put(key, this.get(key));
    }
    // keys.forEach(key -> data.put(key, this.get(key)));

    return data;
  }

  @Override
  public boolean contains(String key) {
    if (!this.isConnected()) {
      new Exception("Redis is not connected!").printStackTrace();
      return false;
    }

    return this.getRedisCommandsCache().keys(key + "*").size() > 0;
  }

  @Override
  public int getRemainingTimeFromKey(String key) {
    if (!this.isConnected()) {
      new Exception("Redis is not connected!").printStackTrace();
      return -1;
    }

    if (!this.contains(key)) {
      return -1;
    }

    return Math.toIntExact(this.getRedisCommandsCache().ttl(key));
  }

  @Override
  public void subscribe(PubSubListener listener, String... channels) {
    if (!this.isConnected()) {
      new Exception("Redis is not connected!").printStackTrace();
      return;
    }

    Collection<String> channelList = Arrays.asList(channels);

    this.getConnectionPubsubListener()
        .addListener(
            new RedisPubSubListener<String, String>() {
              @Override
              public void message(String channel, String message) {
                if (!channelList.contains(channel)) {
                  return;
                }

                listener.onMessageReceived(channel, message);
              }

              @Override
              public void message(String pattern, String channel, String message) {}

              @Override
              public void subscribed(String channel, long count) {}

              @Override
              public void psubscribed(String pattern, long count) {}

              @Override
              public void unsubscribed(String channel, long count) {}

              @Override
              public void punsubscribed(String pattern, long count) {}
            });

    this.getConnectionPubsubListener().sync().subscribe(channels);
  }

  @Override
  public void publish(String channel, String message) {
    if (!this.isConnected()) {
      new Exception("Redis is not connected!").printStackTrace();
      return;
    }

    if (!this.channelExists(channel)) {
      // new Exception("Redis PubSub Channel " + channel + " doesnt exist!").printStackTrace();
      return;
    }

    this.getRedisCommandsPubsubPublish().publish(channel, message);
  }

  @Override
  public boolean channelExists(String channel) {
    if (!this.isConnected()) {
      new Exception("Redis is not connected!").printStackTrace();
      return false;
    }

    return this.getRedisCommandsPubsubPublish().pubsubChannels(channel).contains(channel);
  }
}
