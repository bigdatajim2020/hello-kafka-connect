package us.yellosoft.hellokafkaconnect;

import org.apache.kafka.connect.sink.SinkConnector;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigDef.Type;
import org.apache.kafka.common.config.ConfigDef.Range;
import org.apache.kafka.common.config.ConfigDef.Importance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Manages GreeterTask's
 */
public final class GreeterSink extends SinkConnector {
  private static final Logger LOG = LoggerFactory.getLogger(GreeterSink.class);

  private URI redisAddress;
  private String greetingListKey;

  @Override
  public Class<? extends Task> taskClass() {
    LOG.info("GreeterSink#taskClass");

    return GreeterTask.class;
  }

  @Override
  public ConfigDef config() {
    LOG.info("GreeterSink#config");

    final ConfigDef configDef = new ConfigDef();
    configDef.define(Constants.CONFIG_REDIS_ADDRESS, Type.STRING, "redis://localhost:6379", Importance.HIGH, "Redis address (redis://<host>:<port>)");
    configDef.define(Constants.CONFIG_GREETING_LIST_KEY, Type.STRING, "greetings", Importance.HIGH, "Redis key for greeting list");

    return configDef;
  }

  @Override
  public List<Map<String, String>> taskConfigs(final int maxTasks) {
    LOG.info("GreeterSink#taskConfigs(maxTasks=" + maxTasks + ")");

    final List<Map<String, String>> configs = new LinkedList<>();

    for (int i = 0; i < maxTasks; i++) {
      final Map<String, String> config = new HashMap<>();
      config.put(Constants.CONFIG_REDIS_ADDRESS, redisAddress.toString());
      config.put(Constants.CONFIG_GREETING_LIST_KEY, greetingListKey);

      configs.add(config);
    }

    return configs;
  }

  @Override
  public void start(final Map<String, String> props) {
    LOG.info("GreeterSink#start(props=" + props + ")");

    try {
      redisAddress = new URI(props.get(Constants.CONFIG_REDIS_ADDRESS));
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }

    greetingListKey = props.get(Constants.CONFIG_GREETING_LIST_KEY);
  }

  @Override
  public void stop() {
    LOG.info("GreeterSink#stop");
  }

  @Override
  public String version() {
    LOG.info("GreeterSink#version");

    return Constants.VERSION;
  }
}
