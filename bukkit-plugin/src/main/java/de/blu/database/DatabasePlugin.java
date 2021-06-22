package de.blu.database;

import de.blu.database.data.Platform;
import org.bukkit.plugin.java.JavaPlugin;

import javax.inject.Singleton;
import java.io.File;

@Singleton
public final class DatabasePlugin extends JavaPlugin {

  @Override
  public void onEnable() {
    DatabaseAPI.init(
        Platform.BUKKIT_16, new File(this.getDataFolder(), "libs"), this.getDataFolder());
    DatabaseAPI databaseAPI = DatabaseAPI.getInstance();
  }
}
