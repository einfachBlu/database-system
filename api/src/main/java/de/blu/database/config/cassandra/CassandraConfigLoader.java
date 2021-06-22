package de.blu.database.config.cassandra;

import java.io.File;

public interface CassandraConfigLoader {
  void init(CassandraConfig cassandraConfig, File configDirectory);

  void load();

  void save();
}
