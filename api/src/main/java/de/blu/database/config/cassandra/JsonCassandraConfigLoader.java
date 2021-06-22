package de.blu.database.config.cassandra;

import com.google.gson.Gson;
import lombok.Getter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

@Getter
public final class JsonCassandraConfigLoader implements CassandraConfigLoader {

  private CassandraConfig cassandraConfig;
  private File configFile;
  private Gson gson = new Gson();

  @Override
  public void init(CassandraConfig cassandraConfig, File configDirectory) {
    if (!configDirectory.exists()) {
      configDirectory.mkdirs();
    }

    this.cassandraConfig = cassandraConfig;
    this.configFile = new File(configDirectory, "cassandra.json");

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
      CassandraConfig data = this.gson.fromJson(fileReader, JsonCassandraConfig.class);
      if (data == null) {
        this.save();
        this.load();
        return;
      }

      this.cassandraConfig.copyFrom(data);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void save(){
    try {
      FileWriter fileWriter = new FileWriter(this.configFile);
      fileWriter.write(new Gson().newBuilder().setPrettyPrinting().create().toJson(this.cassandraConfig));
      fileWriter.flush();
      fileWriter.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
