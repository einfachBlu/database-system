package de.blu.database.config.redis;

import com.google.gson.Gson;
import lombok.Getter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

@Getter
public final class JsonRedisConfigLoader implements RedisConfigLoader {

  private RedisConfig redisConfig;
  private File configFile;
  private Gson gson = new Gson();

  @Override
  public void init(RedisConfig redisConfig, File configDirectory) {
    if (!configDirectory.exists()) {
      configDirectory.mkdirs();
    }

    this.redisConfig = redisConfig;
    this.configFile = new File(configDirectory, "redis.json");

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
      RedisConfig data = this.gson.fromJson(fileReader, JsonRedisConfig.class);
      if (data == null) {
        this.save();
        this.load();
        return;
      }

      this.redisConfig.copyFrom(data);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void save() {
    try {
      FileWriter fileWriter = new FileWriter(this.configFile);
      fileWriter.write(
          new Gson().newBuilder().setPrettyPrinting().create().toJson(this.redisConfig));
      fileWriter.flush();
      fileWriter.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
