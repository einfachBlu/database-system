package de.blu.database.storage.pubsub;

import de.blu.database.storage.pubsub.listener.PubSubListener;

public interface PubSub {

  /**
   * Subscribe
   *
   * @param listener the listener for actions when there will be a message published in any of the
   *     channels
   * @param channels the channels which should be subscribed to the listener
   */
  void subscribe(PubSubListener listener, String... channels);

  /**
   * Publish a Message in Redis Pub/Sub Channel
   *
   * @param channel the channel
   * @param message the message
   */
  void publish(String channel, String message);

  /**
   * Check if a Channel was subscribed by any Application
   *
   * @param channel the Channel to check
   * @return true if someone subscribed the channel or false if no one subscribed
   */
  boolean channelExists(String channel);
}
