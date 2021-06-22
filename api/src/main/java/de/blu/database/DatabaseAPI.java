package de.blu.database;

import de.blu.database.config.cassandra.CassandraConfig;
import de.blu.database.config.cassandra.CassandraConfigLoader;
import de.blu.database.config.cassandra.JsonCassandraConfig;
import de.blu.database.config.cassandra.JsonCassandraConfigLoader;
import de.blu.database.config.mysql.JsonMySQLConfig;
import de.blu.database.config.mysql.JsonMySQLConfigLoader;
import de.blu.database.config.mysql.MySQLConfig;
import de.blu.database.config.mysql.MySQLConfigLoader;
import de.blu.database.config.redis.JsonRedisConfig;
import de.blu.database.config.redis.JsonRedisConfigLoader;
import de.blu.database.config.redis.RedisConfig;
import de.blu.database.config.redis.RedisConfigLoader;
import de.blu.database.data.Platform;
import de.blu.database.storage.cassandra.CassandraConnection;
import de.blu.database.storage.cassandra.CassandraConnectionProvider;
import de.blu.database.storage.mysql.MySQLConnection;
import de.blu.database.storage.mysql.MySQLConnectionProvider;
import de.blu.database.storage.redis.RedisConnection;
import de.blu.database.storage.redis.RedisConnectionProvider;
import de.blu.database.util.LibraryUtils;
import lombok.Getter;

import java.io.File;

public final class DatabaseAPI {

  @Getter private static DatabaseAPI instance;

  private CassandraConfigLoader cassandraConfigLoader;
  @Getter private CassandraConfig cassandraConfig;
  @Getter private CassandraConnection cassandraConnection;

  private MySQLConfigLoader mySQLConfigLoader;
  @Getter private MySQLConfig mySQLConfig;
  @Getter private MySQLConnection mySQLConnection;

  private RedisConfigLoader redisConfigLoader;
  @Getter private RedisConfig redisConfig;
  @Getter private RedisConnection redisConnection;

  private Platform platform;
  private File libsDirectory;
  private File configDirectory;

  public DatabaseAPI(Platform platform, File libsDirectory, File configDirectory) {
    this.platform = platform;
    this.libsDirectory = libsDirectory;
    this.configDirectory = configDirectory;

    DatabaseAPI.instance = this;
  }

  public static void init(Platform platform, File libsDirectory, File configDirectory) {
    DatabaseAPI databaseAPI = new DatabaseAPI(platform, libsDirectory, configDirectory);
    databaseAPI.loadLibraries();
    databaseAPI.loadConfigs();
  }

  public void loadLibraries() {
    LibraryUtils.init(this.libsDirectory);
    LibraryUtils.loadLibraries(this.platform);
  }

  public void loadConfigs() {
    // Cassandra
    this.cassandraConfig = new JsonCassandraConfig();
    this.cassandraConfigLoader = new JsonCassandraConfigLoader();

    this.cassandraConfigLoader.init(this.cassandraConfig, this.configDirectory);
    this.cassandraConfigLoader.load();

    if (this.cassandraConfig.isEnabled()) {
      this.cassandraConnection = new CassandraConnectionProvider();
      this.cassandraConnection.init(this.cassandraConfig);

      try {
        System.out.println("Connecting to Cassandra...");
        this.cassandraConnection.connect();
      } catch (Exception e) {
        e.printStackTrace();
      }

      if (this.cassandraConnection.isConnected()) {
        System.out.println("Successfully connected to Cassandra.");
      } else {
        System.out.println("Could not connect to Cassandra.");
      }
    }

    // MySQL
    this.mySQLConfig = new JsonMySQLConfig();
    this.mySQLConfigLoader = new JsonMySQLConfigLoader();

    this.mySQLConfigLoader.init(this.mySQLConfig, this.configDirectory);
    this.mySQLConfigLoader.load();

    if (this.mySQLConfig.isEnabled()) {
      this.mySQLConnection = new MySQLConnectionProvider();
      this.mySQLConnection.init(this.mySQLConfig);

      try {
        System.out.println("Connecting to MySQL...");
        this.mySQLConnection.connect();
      } catch (Exception e) {
        e.printStackTrace();
      }

      if (this.mySQLConnection.isConnected()) {
        System.out.println("Successfully connected to MySQL.");
      } else {
        System.out.println("Could not connect to MySQL.");
      }
    }

    // Redis
    this.redisConfig = new JsonRedisConfig();
    this.redisConfigLoader = new JsonRedisConfigLoader();

    this.redisConfigLoader.init(this.redisConfig, this.configDirectory);
    this.redisConfigLoader.load();

    if (this.redisConfig.isEnabled()) {
      this.redisConnection = new RedisConnectionProvider();
      this.redisConnection.init(this.redisConfig);

      try {
        System.out.println("Connecting to Redis...");
        this.redisConnection.connect();
      } catch (Exception e) {
        e.printStackTrace();
      }

      if (this.redisConnection.isConnected()) {
        System.out.println("Successfully connected to Redis.");
      } else {
        System.out.println("Could not connect to Redis.");
      }
    }
  }
}
