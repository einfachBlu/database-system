package de.blu.database.storage;

public interface Storage {

  /**
   * Connect to the Storage
   */
  void connect();

  /**
   * Disconnect from the Storage
   */
  void disconnect();

  /**
   * Check if connected to the Storage
   *
   * @return true if connected or false if not
   */
  boolean isConnected();
}
