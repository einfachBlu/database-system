package de.blu.database.config.cassandra;

import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;

@Getter
@Setter
public final class JsonCassandraConfig implements CassandraConfig {

  private boolean enabled = false;
  private List<String> hosts = Arrays.asList("localhost", "127.0.0.1");
  private String keySpaceName = "network";
  private String userName = "cassandra";
  private String password = "cassandra";

  @Override
  public void copyFrom(CassandraConfig cassandraConfig) {
    this.enabled = cassandraConfig.isEnabled();
    this.hosts = cassandraConfig.getHosts();
    this.keySpaceName = cassandraConfig.getKeySpaceName();
    this.userName = cassandraConfig.getUserName();
    this.password = cassandraConfig.getPassword();
  }
}
