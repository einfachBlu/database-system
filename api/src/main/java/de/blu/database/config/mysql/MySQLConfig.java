package de.blu.database.config.mysql;

public interface MySQLConfig {
  boolean isEnabled();

  String getHost();

  int getPort();

  String getUserName();

  String getPassword();

  String getDatabase();

  void setEnabled(boolean value);

  void setHost(String host);

  void setPort(int port);

  void setUserName(String userName);

  void setPassword(String password);

  void setDatabase(String database);

  void copyFrom(MySQLConfig mySQLConfig);
}
