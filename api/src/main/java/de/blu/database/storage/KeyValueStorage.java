package de.blu.database.storage;

import java.util.Collection;
import java.util.Map;

public interface KeyValueStorage extends Storage {

  /**
   * Set the value of the Key
   *
   * @param key the key
   * @param value the new Value to set
   */
  void set(String key, String value);

  /**
   * Set the value of the Key and expire after a specified time
   *
   * @param key the key
   * @param value the new Value to set
   * @param expireSeconds the time in seconds when the key should expire
   */
  void set(String key, String value, int expireSeconds);

  /**
   * Remove a Key from Storage
   *
   * @param key the Key to remove
   */
  void remove(String key);

  /**
   * Get all child Keys of a given Key (not recursive)
   *
   * @param key the parent Key
   * @return Collection with all Keys
   */
  Collection<String> getKeys(String key);

  /**
   * Get all child Keys of a given Key
   *
   * @param key the parent Key
   * @param recursive whether it should be returned all keys recursive or not
   * @return Collection with all Keys
   */
  Collection<String> getKeys(String key, boolean recursive);

  /**
   * Get the String value of a specified Key
   *
   * @param key the Key where the Value should get from
   * @return String with the value from the Key or null if the key does not exist
   */
  String get(String key);

  /**
   * Get a Map with all Data saved in Storage
   *
   * @return Map with all Keys and their Values
   */
  Map<String, String> getAll();

  /**
   * Check if a specified Key exist in Storage
   *
   * @param key the Key to check
   * @return true if the Key exist or false if not
   */
  boolean contains(String key);

  /**
   * Get the remaining Time until the Key expire
   *
   * @param key the Key to get the remaining time from
   * @return the time in seconds, until the Key will expire
   */
  int getRemainingTimeFromKey(String key);
}
