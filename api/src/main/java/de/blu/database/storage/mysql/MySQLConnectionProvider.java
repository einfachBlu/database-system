package de.blu.database.storage.mysql;

import de.blu.database.config.mysql.MySQLConfig;
import de.blu.database.data.TableColumn;
import lombok.Getter;

import javax.inject.Singleton;
import java.sql.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@Singleton
@Getter
public final class MySQLConnectionProvider implements MySQLConnection {

  private Connection connection;
  private ExecutorService executorService = Executors.newCachedThreadPool();

  private String host;
  private int port;
  private String userName;
  private String password;
  private String database;

  @Override
  public void init(MySQLConfig mySQLConfig) {
    this.host = mySQLConfig.getHost();
    this.port = mySQLConfig.getPort();
    this.userName = mySQLConfig.getUserName();
    this.password = mySQLConfig.getPassword();
    this.database = mySQLConfig.getDatabase();
  }

  @Override
  public void connect() {
    try {
      this.connection =
          DriverManager.getConnection(
              "jdbc:mysql://"
                  + this.host
                  + ":"
                  + this.port
                  + "/"
                  + this.database
                  + "?autoReconnect=true&useSSL=false",
              this.userName,
              this.password);
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void disconnect() {
    if (!this.isConnected()) {
      return;
    }

    try {
      this.getConnection().close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public boolean isConnected() {
    if (this.getConnection() == null) {
      return false;
    }

    try {
      return !this.getConnection().isClosed();
    } catch (SQLException throwables) {
      throwables.printStackTrace();
    }

    return false;
  }

  @Override
  public Map<Integer, Map<String, Object>> getData(String query) {
    ResultSet resultSet = this.query(query);
    Map<Integer, Map<String, Object>> data = new LinkedHashMap<>();

    if (resultSet == null) {
      return data;
    }

    try {
      ResultSetMetaData md = resultSet.getMetaData();
      int columns = md.getColumnCount();

      int i = 0;

      while (resultSet.next()) {
        Map<String, Object> row = new HashMap<>();
        for (int j = 1; j <= columns; ++j) {
          row.put(md.getColumnName(j), resultSet.getObject(j));
        }

        data.put(i++, row);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    try {
      resultSet.close();
    } catch (SQLException e) {
      e.printStackTrace();
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

    return this.getData("SELECT " + keysString + " FROM " + tableName + ";");
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
        "SELECT " + keysString + " FROM " + tableName + " WHERE " + whereKey + " = " + whereValue);
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
    return this.getData("SELECT * FROM " + tableName + ";");
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
            + tableName
            + " WHERE "
            + whereKey
            + " = "
            + whereValue
            + ";");
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
      new Exception("MySQL is not connected!").printStackTrace();
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
                MySQLConnectionProvider.this.update(tableName, keys, values, whereKey, whereValue));
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
        "INSERT INTO " + tableName + "(" + keysString + ") VALUES(" + valuesString + ");";

    if (!this.isConnected()) {
      System.out.println("Failed SQL Query '" + cqlString + "' MySQL not connected!");
      return;
    }

    this.update(cqlString);
  }

  @Override
  public void insertIntoAsync(String tableName, String[] keys, Object[] values) {
    this.getExecutorService()
        .execute(() -> MySQLConnectionProvider.this.insertInto(tableName, keys, values));
  }

  @Override
  public void deleteFrom(String tableName, String whereKey, Object whereValue) {
    whereValue = whereValue instanceof String ? "'" + whereValue + "'" : whereValue;
    String cqlString = "DELETE FROM " + tableName + " WHERE " + whereKey + " = " + whereValue + ";";

    if (!this.isConnected()) {
      System.out.println("Failed SQL Query '" + cqlString + "' MySQL not connected!");
      return;
    }

    this.update(cqlString);
  }

  @Override
  public void deleteFromAsync(String tableName, String whereKey, Object whereValue) {
    this.getExecutorService()
        .execute(() -> MySQLConnectionProvider.this.deleteFrom(tableName, whereKey, whereValue));
  }

  @Override
  public void createTableIfNotExist(String tableName, List<TableColumn> columns) {
    String query = "CREATE TABLE IF NOT EXISTS " + tableName;

    query += "(";

    for (TableColumn column : columns) {
      if (query.charAt(query.length() - 1) != '(') {
        query += ", ";
      }

      String type = "";
      switch (column.getColumnType()) {
        case UUID:
          throw new IllegalArgumentException("The Type UUID is not valid in MySQL Storage!");
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
      Statement statement = this.connection.createStatement();
      statement.executeUpdate(query);
      statement.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void updateAsync(String query) {
    this.getExecutorService().execute(() -> MySQLConnectionProvider.this.update(query));
  }

  public ResultSet query(String query) {
    try {
      Statement statement = this.connection.createStatement();
      return statement.executeQuery(query);
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return null;
  }

  public void queryAsync(String query, Consumer<ResultSet> consumer) {
    this.getExecutorService()
        .execute(() -> consumer.accept(MySQLConnectionProvider.this.query(query)));
  }
}
