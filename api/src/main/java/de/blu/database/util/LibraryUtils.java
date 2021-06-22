package de.blu.database.util;

import de.blu.database.data.Platform;
import lombok.Getter;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Objects;

public final class LibraryUtils {

  @Getter private static File libraryDirectory;

  public static void init(File libraryDirectory) {
    LibraryUtils.libraryDirectory = libraryDirectory;
    if (!libraryDirectory.exists()) {
      libraryDirectory.mkdirs();
    }
  }

  /** Load Libraries in the LibraryFolder and download the default Libraries */
  public static void loadLibraries(Platform platform) {
    if (!LibraryUtils.libraryDirectory.exists()) {
      return;
    }

    // Cassandra worked with netty 4.0.44

    // Download libraries
    LibraryUtils.downloadLibrary(
        "https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.25/mysql-connector-java-8.0.25.jar");
    LibraryUtils.downloadLibrary(
        "https://repo1.maven.org/maven2/com/datastax/cassandra/cassandra-driver-core/3.2.0/cassandra-driver-core-3.2.0.jar");
    LibraryUtils.downloadLibrary(
        "https://repo1.maven.org/maven2/org/slf4j/slf4j-api/1.7.31/slf4j-api-1.7.31.jar");
    LibraryUtils.downloadLibrary(
        "https://repo1.maven.org/maven2/log4j/log4j/1.2.17/log4j-1.2.17.jar");
    LibraryUtils.downloadLibrary(
        "https://repo1.maven.org/maven2/com/google/guava/guava/19.0/guava-19.0.jar");
    LibraryUtils.downloadLibrary(
        "https://repo1.maven.org/maven2/io/netty/netty-handler/4.1.45.Final/netty-handler-4.1.45.Final.jar");
    LibraryUtils.downloadLibrary(
        "https://repo1.maven.org/maven2/io/netty/netty-common/4.1.45.Final/netty-common-4.1.45.Final.jar");
    LibraryUtils.downloadLibrary(
        "https://repo1.maven.org/maven2/io/netty/netty-transport/4.1.45.Final/netty-transport-4.1.45.Final.jar");
    LibraryUtils.downloadLibrary(
        "https://repo1.maven.org/maven2/io/netty/netty-codec/4.1.45.Final/netty-codec-4.1.45.Final.jar");
    LibraryUtils.downloadLibrary(
        "https://repo1.maven.org/maven2/io/netty/netty-buffer/4.1.45.Final/netty-buffer-4.1.45.Final.jar");
    LibraryUtils.downloadLibrary(
        "https://repo1.maven.org/maven2/io/dropwizard/metrics/metrics-core/3.1.2/metrics-core-3.1.2.jar");
    LibraryUtils.downloadLibrary(
        "https://repo1.maven.org/maven2/io/lettuce/lettuce-core/5.2.2.RELEASE/lettuce-core-5.2.2.RELEASE.jar");
    LibraryUtils.downloadLibrary(
        "https://repo1.maven.org/maven2/io/projectreactor/reactor-core/3.3.2.RELEASE/reactor-core-3.3.2.RELEASE.jar");
    LibraryUtils.downloadLibrary(
        "https://repo1.maven.org/maven2/org/reactivestreams/reactive-streams/1.0.3/reactive-streams-1.0.3.jar");
    LibraryUtils.downloadLibrary(
        "https://repo1.maven.org/maven2/io/netty/netty-resolver/4.1.45.Final/netty-resolver-4.1.45.Final.jar");

    switch (platform) {
      case BUKKIT_16:
        // LibraryUtils.downloadLibrary("https://repo1.maven.org/maven2/com/datastax/cassandra/cassandra-driver-core/3.0.3/cassandra-driver-core-3.0.3.jar");
        break;
      case BUNGEECORD:
        // LibraryUtils.downloadLibrary("https://repo1.maven.org/maven2/com/datastax/cassandra/cassandra-driver-core/3.8.0/cassandra-driver-core-3.8.0.jar");
        break;
      case STANDALONE:
        // LibraryUtils.downloadLibrary("https://repo1.maven.org/maven2/com/datastax/cassandra/cassandra-driver-core/3.8.0/cassandra-driver-core-3.8.0.jar");
        break;
    }

    // Inject into Classpath
    for (File file : Objects.requireNonNull(LibraryUtils.libraryDirectory.listFiles())) {
      LibraryUtils.loadLibrary(file);
      System.out.println("Loaded Library: " + file.getName());
    }
  }

  /**
   * Load Library from File to Classpath
   *
   * @param file the jar File to load
   */
  public static void loadLibrary(File file) {
    try {
      URL url = file.toURI().toURL();

      URLClassLoader classLoader = (URLClassLoader) LibraryUtils.class.getClassLoader();
      Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
      method.setAccessible(true);
      method.invoke(classLoader, url);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Download a Library for loading it later in the classpath
   *
   * @param urlString the url of the library
   */
  public static void downloadLibrary(String urlString) {
    String[] split = urlString.split("/");
    downloadLibrary(urlString, split[split.length - 1]);
  }

  /**
   * Download a Library for loading it later in the classpath
   *
   * @param urlString the url of the library
   * @param fileName the target fileName of the Library
   */
  public static void downloadLibrary(String urlString, String fileName) {
    if (LibraryUtils.libraryDirectory == null) {
      return;
    }

    File targetFile = new File(LibraryUtils.libraryDirectory, fileName);
    try {
      if (!targetFile.exists()) {
        FileUtils.copyURLToFile(new URL(urlString), targetFile);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
