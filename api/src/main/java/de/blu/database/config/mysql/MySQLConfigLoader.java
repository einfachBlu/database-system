package de.blu.database.config.mysql;

import java.io.File;

public interface MySQLConfigLoader {
  void init(MySQLConfig mySQLConfig, File configDirectory);

  void load();

  void save();
}
