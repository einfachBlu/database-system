package de.blu.database.storage.mysql;

import de.blu.database.config.mysql.MySQLConfig;
import de.blu.database.storage.TableStorage;

public interface MySQLConnection extends TableStorage {

  void init(MySQLConfig mySQLConfig);
}
