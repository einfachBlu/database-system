package de.blu.database.storage.pubsub.listener;

public interface PubSubListener {
  /**
   * Will be called every time when a new message was published in the subscribed channel
   *
   * @param channel the channel
   * @param message the message
   */
  void onMessageReceived(String channel, String message);
}
