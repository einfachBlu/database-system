package de.blu.database.config.mysql;

import com.google.gson.Gson;
import lombok.Getter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

@Getter
public final class JsonMySQLConfigLoader implements MySQLConfigLoader {

  private MySQLConfig mySQLConfig;
  private File configFile;
  private Gson gson = new Gson();

  @Override
  public void init(MySQLConfig mySQLConfig, File configDirectory) {
    if (!configDirectory.exists()) {
      configDirectory.mkdirs();
    }

    this.mySQLConfig = mySQLConfig;
    this.configFile = new File(configDirectory, "mysql.json");

    if (!this.configFile.exists()) {
      try {
        this.configFile.createNewFile();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  public void load() {
    try (FileReader fileReader = new FileReader(this.configFile)) {
      MySQLConfig data = this.gson.fromJson(fileReader, JsonMySQLConfig.class);
      if (data == null) {
        this.save();
        this.load();
        return;
      }

      this.mySQLConfig.copyFrom(data);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void save() {
    try {
      FileWriter fileWriter = new FileWriter(this.configFile);
      fileWriter.write(
          new Gson().newBuilder().setPrettyPrinting().create().toJson(this.mySQLConfig));
      fileWriter.flush();
      fileWriter.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
