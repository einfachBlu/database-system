package de.blu.database.storage.cassandra;

import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.Session;
import de.blu.database.config.cassandra.CassandraConfig;
import de.blu.database.data.TableColumn;
import de.blu.database.storage.TableStorage;

import java.util.List;

public interface CassandraConnection extends TableStorage {

  void init(CassandraConfig cassandraConfig);

  /**
   * Get Cassandra Session
   *
   * @return Session
   */
  Session getSession();

  /**
   * Get Data of Keyspace
   *
   * @return KeyspaceMetadata
   */
  KeyspaceMetadata getKeyspace();

  /**
   * Get the Name of the Keyspace
   *
   * @return the name of the Keyspace
   */
  String getKeyspaceName();

  /**
   * Create Table in the Storage if not exist
   *
   * @param customKeySpace KeySpace for the table.
   * @param tableName name of the table
   * @param columns the columns
   */
  void createTableIfNotExist(String customKeySpace, String tableName, List<TableColumn> columns);
}
