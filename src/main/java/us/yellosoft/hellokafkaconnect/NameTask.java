package us.yellosoft.hellokafkaconnect;

import org.apache.kafka.connect.source.SourceTask;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.data.Schema;

import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;

/**
 * Sends names to Kafka
 */
public final class NameTask extends SourceTask {
  private Random random;
  private String kafkaTopic;
  private int kafkaPartitions;
  private URI redisAddress;
  private String nameListKey;

  private Jedis jedis;

  @Override
  public void start(final Map<String, String> props) {
    random = new Random();

    kafkaTopic = props.get(Constants.CONFIG_KAFKA_TOPIC);
    kafkaPartitions = Integer.parseInt(props.get(Constants.CONFIG_KAFKA_PARTITIONS));

    try {
      redisAddress = new URI(props.get(Constants.CONFIG_REDIS_ADDRESS));
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }

    nameListKey = props.get(Constants.CONFIG_NAME_LIST_KEY);

    jedis = new Jedis(redisAddress);
  }

  @Override
  public List<SourceRecord> poll() {
    final List<SourceRecord> records = new LinkedList<>();

    final String name = jedis.lpop(nameListKey);
    final byte[] message = name.getBytes(Charset.forName("UTF-8"));
    final SourceRecord record = new SourceRecord(null, null, kafkaTopic, random.nextInt(kafkaPartitions), Schema.BYTES_SCHEMA, message);

    records.add(record);

    return records;
  }

  @Override
  public void stop() {
    jedis.disconnect();
  }

  @Override
  public String version() {
    return Constants.VERSION;
  }
}
