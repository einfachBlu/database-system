package de.blu.database.storage.cassandra;

import com.datastax.driver.core.*;
import com.datastax.driver.core.exceptions.DriverException;
import de.blu.database.config.cassandra.CassandraConfig;
import de.blu.database.data.TableColumn;
import lombok.Getter;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@Singleton
@Getter
public final class CassandraConnectionProvider implements CassandraConnection {

  private Session session;
  private Cluster cluster;
  private KeyspaceMetadata keyspace;
  private String keyspaceName;
  private ExecutorService executorService = Executors.newCachedThreadPool();
  private Cluster.Builder clusterBuilder;

  @Override
  public void init(CassandraConfig cassandraConfig) {
    this.keyspaceName = cassandraConfig.getKeySpaceName();
    this.clusterBuilder = new Cluster.Builder();

    for (String address : cassandraConfig.getHosts()) {
      this.clusterBuilder.addContactPoint(address);
    }

    this.clusterBuilder.withCredentials(
        cassandraConfig.getUserName(), cassandraConfig.getPassword());

    this.cluster = this.clusterBuilder.build();
  }

  @Override
  public void connect() {
    this.session = this.cluster.connect();
    this.keyspace = cluster.getMetadata().getKeyspace(this.getKeyspaceName());

    // Create KeySpace if doesn't exist
    if (this.getKeyspace() == null) {
      this.getSession()
          .execute(
              "CREATE KEYSPACE "
                  + this.getKeyspaceName()
                  + " WITH REPLICATION = {'class': 'SimpleStrategy', 'replication_factor': 3};");
      this.keyspace = cluster.getMetadata().getKeyspace(this.getKeyspaceName());
    }

    this.getSession().execute("USE " + this.getKeyspaceName() + ";");
  }

  @Override
  public void disconnect() {
    if (!this.isConnected()) {
      return;
    }

    this.getSession().close();
  }

  @Override
  public boolean isConnected() {
    return this.getSession() != null && !this.getSession().isClosed();
  }

  @Override
  public Map<Integer, Map<String, Object>> getData(String query) {
    ResultSet resultSet = this.query(query);
    Map<Integer, Map<String, Object>> data = new LinkedHashMap<>();

    if (resultSet == null) {
      return data;
    }

    int i = 0;
    for (Row row : resultSet) {
      Map<String, Object> rowData = new HashMap<>();

      for (ColumnDefinitions.Definition column : row.getColumnDefinitions().asList()) {
        rowData.put(column.getName(), row.getObject(column.getName()));
      }

      data.put(i, rowData);
      i++;
    }

    return data;
  }

  @Override
  public Map<Integer, Map<String, Object>> select(String tableName, String[] keys) {
    StringBuilder keysStringBuilder = new StringBuilder();
    for (String key : keys) {
      keysStringBuilder.append(", ").append(key);
    }
    String keysString = keysStringBuilder.toString();
    if (keysString.length() > 2) {
      keysString = keysString.substring(2);
    }

    return this.getData(
        "SELECT " + keysString + " FROM " + this.getKeyspaceName() + "." + tableName + ";");
  }

  @Override
  public void selectAsync(
      String tableName, String[] keys, Consumer<Map<Integer, Map<String, Object>>> consumer) {
    this.getExecutorService()
        .execute(
            () -> {
              consumer.accept(this.select(tableName, keys));
            });
  }

  @Override
  public Map<Integer, Map<String, Object>> select(
      String tableName, String[] keys, String whereKey, Object whereValue) {
    StringBuilder keysStringBuilder = new StringBuilder();
    for (String key : keys) {
      keysStringBuilder.append(", ").append(key);
    }
    String keysString = keysStringBuilder.toString();
    if (keysString.length() > 2) {
      keysString = keysString.substring(2);
    }

    whereValue = whereValue instanceof String ? "'" + whereValue + "'" : whereValue;

    return this.getData(
        "SELECT "
            + keysString
            + " FROM "
            + this.getKeyspaceName()
            + "."
            + tableName
            + " WHERE "
            + whereKey
            + " = "
            + whereValue
            + " ALLOW FILTERING;");
  }

  @Override
  public void selectAsync(
      String tableName,
      String[] keys,
      String whereKey,
      Object whereValue,
      Consumer<Map<Integer, Map<String, Object>>> consumer) {
    this.getExecutorService()
        .execute(
            () -> {
              consumer.accept(this.select(tableName, keys, whereKey, whereValue));
            });
  }

  @Override
  public Map<Integer, Map<String, Object>> selectAll(String tableName) {
    return this.getData("SELECT * FROM " + this.getKeyspaceName() + "." + tableName + ";");
  }

  @Override
  public void selectAllAsync(
      String tableName, Consumer<Map<Integer, Map<String, Object>>> consumer) {
    this.getExecutorService()
        .execute(
            () -> {
              consumer.accept(this.selectAll(tableName));
            });
  }

  @Override
  public Map<Integer, Map<String, Object>> selectAll(
      String tableName, String whereKey, Object whereValue) {
    whereValue = whereValue instanceof String ? "'" + whereValue + "'" : whereValue;
    return this.getData(
        "SELECT * FROM "
            + this.getKeyspaceName()
            + "."
            + tableName
            + " WHERE "
            + whereKey
            + " = "
            + whereValue
            + " ALLOW FILTERING;");
  }

  @Override
  public void selectAllAsync(
      String tableName,
      String whereKey,
      Object whereValue,
      Consumer<Map<Integer, Map<String, Object>>> consumer) {
    this.getExecutorService()
        .execute(
            () -> {
              consumer.accept(this.selectAll(tableName, whereKey, whereValue));
            });
  }

  @Override
  public void update(
      String tableName, String[] keys, Object[] values, String whereKey, Object whereValue) {
    if (keys.length != values.length) {
      return;
    }

    if (!isConnected()) {
      new Exception("Cassandra is not connected!").printStackTrace();
      return;
    }

    StringBuilder updateStringBuilder = new StringBuilder();
    for (int i = 0; i < keys.length; i++) {
      values[i] = values[i] instanceof String ? "'" + values[i] + "'" : values[i];
      updateStringBuilder.append(", ").append(keys[i]).append(" = ").append(values[i]);
    }
    String updateString = updateStringBuilder.toString();
    if (updateString.length() > 2) {
      updateString = updateString.substring(2);
    }

    whereValue = whereValue instanceof String ? "'" + whereValue + "'" : whereValue;
    String cqlString =
        "UPDATE "
            + this.getKeyspaceName()
            + "."
            + tableName
            + " SET "
            + updateString
            + " WHERE "
            + whereKey
            + " = "
            + whereValue
            + ";";

    this.update(cqlString);
  }

  @Override
  public void updateAsync(
      String tableName, String[] keys, Object[] values, String whereKey, Object whereValue) {
    this.getExecutorService()
        .execute(
            () ->
                CassandraConnectionProvider.this.update(
                    tableName, keys, values, whereKey, whereValue));
  }

  @Override
  public void insertInto(String tableName, String[] keys, Object[] values) {
    String keysString;
    String valuesString;

    StringBuilder keysStringBuilder = new StringBuilder();
    StringBuilder valuesStringBuilder = new StringBuilder();
    for (String key : keys) {
      keysStringBuilder.append(", ").append(key);
    }
    keysString = keysStringBuilder.toString();
    if (keysString.length() > 2) {
      keysString = keysString.substring(2);
    }

    for (Object value : values) {
      value = value instanceof String ? "'" + value + "'" : value;
      valuesStringBuilder.append(", ").append(value);
    }
    valuesString = valuesStringBuilder.toString();
    if (valuesString.length() > 2) {
      valuesString = valuesString.substring(2);
    }

    String cqlString =
        "INSERT INTO "
            + this.getKeyspaceName()
            + "."
            + tableName
            + "("
            + keysString
            + ") VALUES("
            + valuesString
            + ");";

    if (!this.isConnected()) {
      System.out.println("Failed CQL '" + cqlString + "' Cassandra not connected!");
      return;
    }

    this.update(cqlString);
  }

  @Override
  public void insertIntoAsync(String tableName, String[] keys, Object[] values) {
    this.getExecutorService()
        .execute(() -> CassandraConnectionProvider.this.insertInto(tableName, keys, values));
  }

  @Override
  public void deleteFrom(String tableName, String whereKey, Object whereValue) {
    whereValue = whereValue instanceof String ? "'" + whereValue + "'" : whereValue;
    String cqlString =
        "DELETE FROM "
            + this.getKeyspaceName()
            + "."
            + tableName
            + " WHERE "
            + whereKey
            + " = "
            + whereValue
            + ";";

    if (!this.isConnected()) {
      System.out.println("Failed CQL '" + cqlString + "' Cassandra not connected!");
      return;
    }

    this.update(cqlString);
  }

  @Override
  public void deleteFromAsync(String tableName, String whereKey, Object whereValue) {
    this.getExecutorService()
        .execute(
            () -> CassandraConnectionProvider.this.deleteFrom(tableName, whereKey, whereValue));
  }

  @Override
  public void createTableIfNotExist(String tableName, List<TableColumn> columns) {
    this.createTableIfNotExist(this.getKeyspaceName(), tableName, columns);
  }

  @Override
  public void createTableIfNotExist(String customKeySpace, String tableName, List<TableColumn> columns) {
    String query = "CREATE TABLE IF NOT EXISTS " + customKeySpace + "." + tableName;

    query += "(";

    for (TableColumn column : columns) {
      if (query.charAt(query.length() - 1) != '(') {
        query += ", ";
      }

      String type = "";
      switch (column.getColumnType()) {
        case UUID:
          type = "uuid";
          break;
        case BOOLEAN:
          type = "boolean";
          break;
        case INTEGER:
          type = "int";
          break;
        case BIGINT:
          type = "bigint";
          break;
        case STRING:
          type = "text";
          break;
      }

      query += column.getName() + " " + type;
    }

    if (columns.stream().anyMatch(TableColumn::isPrimaryKey)) {
      query += ", PRIMARY KEY (";

      for (TableColumn column : columns) {
        if (!column.isPrimaryKey()) {
          continue;
        }

        if (query.charAt(query.length() - 1) != '(') {
          query += ", ";
        }

        query += column.getName();
      }

      query += ")";
    }

    query += ");";

    this.update(query);
  }

  public void update(String query) {
    try {
      this.getSession().execute(query);
      /*
      PreparedStatement preparedStatement = this.getSession().prepare(query);
      this.getSession().execute(preparedStatement.bind());
       */
    } catch (DriverException e) {
      e.printStackTrace();
    }
  }

  public void updateAsync(String query) {
    this.getExecutorService().execute(() -> CassandraConnectionProvider.this.update(query));
  }

  public ResultSet query(String query) {
    try {
      return this.getSession().execute(query);
      /*
      PreparedStatement preparedStatement = this.getSession().prepare(query);
      return this.getSession().execute(preparedStatement.bind());
       */
    } catch (DriverException e) {
      e.printStackTrace();
    }

    return null;
  }

  public void queryAsync(String query, Consumer<ResultSet> consumer) {
    this.getExecutorService()
        .execute(() -> consumer.accept(CassandraConnectionProvider.this.query(query)));
  }
}
