package us.yellosoft.hellokafkaconnect;

import org.apache.kafka.connect.sink.SinkTask;
import org.apache.kafka.connect.sink.SinkRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;

import redis.clients.jedis.Jedis;

import java.util.Collection;
import java.util.Map;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;

/**
 * Welcomes names from Kafka
 */
public final class GreeterTask extends SinkTask {
  private URI redisAddress;
  private String greetingListKey;

  private Jedis jedis;

  @Override
  public void start(final Map<String, String> props) {
    try {
      redisAddress = new URI(props.get(Constants.CONFIG_REDIS_ADDRESS));
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }

    greetingListKey = props.get(Constants.CONFIG_GREETING_LIST_KEY);

    jedis = new Jedis(redisAddress);
    jedis.connect();
  }

  @Override
  public void put(final Collection<SinkRecord> records) {
    for (SinkRecord record : records) {
      final byte[] message = (byte[]) record.value();
      final String name = new String(message, Charset.forName("UTF-8"));

      final String greeting = String.format("Welcome, %s", name);

      if (jedis.isConnected()) {
        jedis.lpush(greetingListKey, greeting);
      }
    }
  }

  @Override
  public void flush(final Map<TopicPartition, OffsetAndMetadata> offsets) {}

  @Override
  public void stop() {
    jedis.disconnect();
  }

  @Override
  public String version() {
    return Constants.VERSION;
  }
}
