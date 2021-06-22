package database;

import de.blu.database.DatabaseAPI;
import de.blu.database.data.Platform;
import de.blu.database.data.TableColumn;
import de.blu.database.data.TableColumnType;
import de.blu.database.storage.cassandra.CassandraConnection;
import de.blu.database.storage.mysql.MySQLConnection;
import de.blu.database.storage.redis.RedisConnection;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public final class DatabaseTest {
  public static void main(String[] args) {
    System.out.println("Testing Database Standalone");
    DatabaseAPI.init(
        Platform.STANDALONE,
        new File("C:\\Users/Blu/IdeaProjects/database-system/bukkit-plugin/build/data/libs"),
        new File("C:\\Users/Blu/IdeaProjects/database-system/bukkit-plugin/build/data/configs"));

    DatabaseAPI databaseAPI = DatabaseAPI.getInstance();

    testCassandra(databaseAPI);
    testMySql(databaseAPI);
    testRedis(databaseAPI);
    // testMongoDb(databaseAPI);
  }

  private static void testRedis(DatabaseAPI databaseAPI) {
    if (databaseAPI.getRedisConfig().isEnabled()) {
      RedisConnection redisConnection = databaseAPI.getRedisConnection();
      if (!redisConnection.isConnected()) {
        System.out.println("Redis not connected");
        return;
      }

      System.out.println(redisConnection.getAll());
      redisConnection.set("test", "Hello World", 3);
      System.out.println(redisConnection.getAll());

      redisConnection.subscribe(
          (channel, message) -> {
            System.out.println(" ");
            System.out.println("Incoming Message!");
            System.out.println("Channel: " + channel);
            System.out.println("Message: " + message);
          }, "Bungee-1", "Lobby-1");

      redisConnection.publish("Lobby-1", "Hallo Lobby 1");
      redisConnection.publish("Bungee-1", "Hallo Bungee 1 ^^");
      redisConnection.publish("Bungee-2", "Hallo Bungee 2 ^^");
    }
  }

  private static void testMySql(DatabaseAPI databaseAPI) {
    if (databaseAPI.getMySQLConfig().isEnabled()) {
      System.out.println("Testing MySQL...");
      MySQLConnection mySQLConnection = databaseAPI.getMySQLConnection();
      if (!mySQLConnection.isConnected()) {
        System.out.println("MySQL not connected");
        return;
      }

      System.out.println(mySQLConnection.selectAll("locations"));
      mySQLConnection.insertInto(
          "locations",
          new String[] {"location_key", "intcounter", "value", "active", "timestamp"},
          new Object[] {"test.key", 5, "somewhere", false, System.currentTimeMillis()});
      System.out.println(mySQLConnection.selectAll("locations"));
      mySQLConnection.deleteFrom("locations", "location_key", "test.key");
      System.out.println(mySQLConnection.selectAll("locations"));
    }
  }

  private static void testCassandra(DatabaseAPI databaseAPI) {
    if (databaseAPI.getCassandraConfig().isEnabled()) {
      System.out.println("Testing Cassandra...");
      CassandraConnection cassandraConnection = databaseAPI.getCassandraConnection();
      if (!cassandraConnection.isConnected()) {
        System.out.println("Cassandra not connected");
        return;
      }

      String customKeySpace = "network";
      String tableName = "locations";

      List<TableColumn> columns =
          Arrays.asList(
              new TableColumn(TableColumnType.STRING, "key", true),
              new TableColumn(TableColumnType.STRING, "value", false),
              new TableColumn(TableColumnType.INTEGER, "intcounter", false),
              new TableColumn(TableColumnType.BIGINT, "timestamp", false),
              new TableColumn(TableColumnType.BOOLEAN, "active", false));

      cassandraConnection.createTableIfNotExist(customKeySpace, tableName, columns);

      System.out.println(cassandraConnection.selectAll("locations", "key", "test.key"));
      cassandraConnection.deleteFrom("locations", "key", "test.key");
      System.out.println(cassandraConnection.selectAll("locations", "key", "test.key"));
    }
  }
}
