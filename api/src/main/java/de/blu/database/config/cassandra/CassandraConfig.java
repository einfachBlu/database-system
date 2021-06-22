package de.blu.database.config.cassandra;

import java.util.List;

public interface CassandraConfig {
  boolean isEnabled();

  List<String> getHosts();

  String getKeySpaceName();

  String getUserName();

  String getPassword();

  void setEnabled(boolean value);

  void setHosts(List<String> hosts);

  void setKeySpaceName(String keySpaceName);

  void setUserName(String userName);

  void setPassword(String password);

  void copyFrom(CassandraConfig cassandraConfig);
}
