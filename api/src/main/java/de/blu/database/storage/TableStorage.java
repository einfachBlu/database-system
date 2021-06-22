package de.blu.database.storage;

import de.blu.database.data.TableColumn;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public interface TableStorage extends Storage {

  /**
   * Get Data from the query as Map
   *
   * @param query the query which should use
   * @return Map with all returned data from the query
   */
  Map<Integer, Map<String, Object>> getData(String query);

  /**
   * Get Data as Map from the Database with the SELECT Query
   *
   * @param tableName the tableName
   * @param keys the keys to get
   * @return Map with all data which was returned
   */
  Map<Integer, Map<String, Object>> select(String tableName, String[] keys);

  /**
   * Get Data Async as Map from the Database with the SELECT Query Async
   *
   * @param tableName the tableName
   * @param keys the keys to get
   * @param consumer the callback for the async action, will be invoked with all returned date
   */
  void selectAsync(
      String tableName, String[] keys, Consumer<Map<Integer, Map<String, Object>>> consumer);

  /**
   * Get Data as Map from the Database with the SELECT Query
   *
   * @param tableName the tableName
   * @param keys the keys to get
   * @param whereKey WHERE whereKey = X
   * @param whereValue WHERE X = whereValue
   * @return Map with all data which was returned
   */
  Map<Integer, Map<String, Object>> select(
      String tableName, String[] keys, String whereKey, Object whereValue);

  /**
   * Get Data Async as Map from the Database with the SELECT Query
   *
   * @param tableName the tableName
   * @param keys the keys to get
   * @param whereKey WHERE whereKey = X
   * @param whereValue WHERE X = whereValue
   * @param consumer the callback for the async action, will be invoked with all returned data
   */
  void selectAsync(
      String tableName,
      String[] keys,
      String whereKey,
      Object whereValue,
      Consumer<Map<Integer, Map<String, Object>>> consumer);

  /**
   * Get Data as Map from the Database with the Command "SELECT *"
   *
   * @param tableName the tableName
   * @return Map with all data which was returned
   */
  Map<Integer, Map<String, Object>> selectAll(String tableName);

  /**
   * Get Data Async as Map from the Database with the Command "SELECT *"
   *
   * @param tableName the tableName
   * @param consumer the callback for the async action, will be invoked with all returned data
   */
  void selectAllAsync(String tableName, Consumer<Map<Integer, Map<String, Object>>> consumer);

  /**
   * Get Data as Map from the Database with the Command "SELECT"
   *
   * @param tableName the tableName
   * @param whereKey WHERE whereKey = X
   * @param whereValue WHERE X = whereValue
   * @return Map with all data which was returned
   */
  Map<Integer, Map<String, Object>> selectAll(String tableName, String whereKey, Object whereValue);

  /**
   * Get Data Async as Map from the Database with the Command "SELECT"
   *
   * @param tableName the tableName
   * @param whereKey WHERE whereKey = X
   * @param whereValue WHERE X = whereValue
   * @param consumer the callback for the async action, will be invoked with all returned data
   */
  void selectAllAsync(
      String tableName,
      String whereKey,
      Object whereValue,
      Consumer<Map<Integer, Map<String, Object>>> consumer);

  /**
   * Update values from a target entry
   *
   * @param tableName the tableName
   * @param keys the keys to set
   * @param values the new values
   * @param whereKey WHERE whereKey = X
   * @param whereValue WHERE X = whereValue
   */
  void update(String tableName, String[] keys, Object[] values, String whereKey, Object whereValue);

  /**
   * Update values Async from a target entry
   *
   * @param tableName the tableName
   * @param keys the keys to set
   * @param values the new values
   * @param whereKey WHERE whereKey = X
   * @param whereValue WHERE X = whereValue
   */
  void updateAsync(
      String tableName, String[] keys, Object[] values, String whereKey, Object whereValue);

  /**
   * Insert data in the table
   *
   * @param tableName the tableName
   * @param keys the keys to set
   * @param values the values
   */
  void insertInto(String tableName, String[] keys, Object[] values);

  /**
   * Insert data in the table Async
   *
   * @param tableName the tableName
   * @param keys the keys to set
   * @param values the values
   */
  void insertIntoAsync(String tableName, String[] keys, Object[] values);

  /**
   * Delete Data from a table
   *
   * @param tableName the tableName
   * @param whereKey WHERE whereKey = X
   * @param whereValue WHERE X = whereValue
   */
  void deleteFrom(String tableName, String whereKey, Object whereValue);

  /**
   * Delete Data Async from a table
   *
   * @param tableName the tableName
   * @param whereKey WHERE whereKey = X
   * @param whereValue WHERE X = whereValue
   */
  void deleteFromAsync(String tableName, String whereKey, Object whereValue);

  /**
   * Create Table in the Storage if not exist
   *
   * @param tableName name of the table
   * @param columns the columns
   */
  void createTableIfNotExist(String tableName, List<TableColumn> columns);
}
