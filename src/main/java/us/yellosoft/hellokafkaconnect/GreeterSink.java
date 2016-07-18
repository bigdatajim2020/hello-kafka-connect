package us.yellosoft.hellokafkaconnect;

import org.apache.kafka.connect.sink.SinkConnector;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigDef.Type;
import org.apache.kafka.common.config.ConfigDef.Range;
import org.apache.kafka.common.config.ConfigDef.Importance;

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
  private String kafkaTopic;
  private int kafkaPartitions;
  private URI redisAddress;
  private String greetingListKey;

  @Override
  public Class<? extends Task> taskClass() {
    return GreeterTask.class;
  }

  @Override
  public ConfigDef config() {
    final ConfigDef configDef = new ConfigDef();
    configDef.define(Constants.CONFIG_KAFKA_TOPIC, Type.STRING, "names", Importance.LOW, "Kafka topic for name messages");
    configDef.define(Constants.CONFIG_KAFKA_PARTITIONS, Type.INT, Range.atLeast(0), Importance.LOW, "Number of Kafka partitions");
    configDef.define(Constants.CONFIG_REDIS_ADDRESS, Type.STRING, "redis://localhost:6379", Importance.HIGH, "Redis address (redis://<host>:<port>)");
    configDef.define(Constants.CONFIG_GREETING_LIST_KEY, Type.STRING, "greetings", Importance.HIGH, "Redis key for greeting list");

    return configDef;
  }

  @Override
  public List<Map<String, String>> taskConfigs(final int maxTasks) {
    final List<Map<String, String>> configs = new LinkedList<>();

    for (int i = 0; i < maxTasks; i++) {
      final Map<String, String> config = new HashMap<>();
      config.put(Constants.CONFIG_KAFKA_TOPIC, kafkaTopic);
      config.put(Constants.CONFIG_KAFKA_PARTITIONS, String.valueOf(kafkaPartitions));
      config.put(Constants.CONFIG_REDIS_ADDRESS, redisAddress.toString());
      config.put(Constants.CONFIG_GREETING_LIST_KEY, greetingListKey);

      configs.add(config);
    }

    return configs;
  }

  @Override
  public void start(final Map<String, String> props) {
    kafkaTopic = props.get(Constants.CONFIG_KAFKA_TOPIC);
    kafkaPartitions = Integer.parseInt(props.get(Constants.CONFIG_KAFKA_PARTITIONS));

    try {
      redisAddress = new URI(props.get(Constants.CONFIG_REDIS_ADDRESS));
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }

    greetingListKey = props.get(Constants.CONFIG_GREETING_LIST_KEY);
  }

  @Override
  public void stop() {}

  @Override
  public String version() {
    return Constants.VERSION;
  }
}
