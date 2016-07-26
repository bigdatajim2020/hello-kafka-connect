package us.yellosoft.hellokafkaconnect;

import org.apache.kafka.connect.sink.SinkTask;
import org.apache.kafka.connect.sink.SinkRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;

import redis.clients.jedis.Jedis;

import org.apache.commons.codec.binary.Hex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;

/**
 * Welcomes names from Kafka
 */
public final class GreeterTask extends SinkTask {
  private static final Logger LOG = LoggerFactory.getLogger(GreeterTask.class);

  private URI redisAddress;
  private String greetingListKey;

  private Jedis jedis;

  @Override
  public void start(final Map<String, String> props) {
    LOG.info("GreeterTask#start(props=" + props + ")");

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
    LOG.info("GreeterTask#put(records=" + records + ")");

    for (SinkRecord record : records) {
      final byte[] message = (byte[]) record.value();

      LOG.info("GreeterTask#put: received message = " + Hex.encodeHexString(message));

      final String name = new String(message, Charset.forName("UTF-8"));

      LOG.info("GreeterTask#put: decoded name = " + name);

      final String greeting = String.format("Welcome, %s", name);

      LOG.info("GreeterTask#put: generated greeting = " + greeting);

      if (jedis.isConnected()) {
        jedis.lpush(greetingListKey, greeting);
      }
    }
  }

  @Override
  public void flush(final Map<TopicPartition, OffsetAndMetadata> offsets) {
    LOG.info("GreeterTask#flush");
  }

  @Override
  public void stop() {
    LOG.info("GreeterTask#stop");

    jedis.disconnect();
  }

  @Override
  public String version() {
    LOG.info("GreeterTask#version");

    return Constants.VERSION;
  }
}
