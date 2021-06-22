package de.blu.database.config.mysql;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class JsonMySQLConfig implements MySQLConfig {

  private boolean enabled = false;
  private String host = "127.0.0.1";
  private int port = 3306;
  private String userName = "root";
  private String password = "123456";
  private String database = "network";

  @Override
  public void copyFrom(MySQLConfig mySQLConfig) {
    this.enabled = mySQLConfig.isEnabled();
    this.host = mySQLConfig.getHost();
    this.port = mySQLConfig.getPort();
    this.userName = mySQLConfig.getUserName();
    this.password = mySQLConfig.getPassword();
    this.database = mySQLConfig.getDatabase();
  }
}
