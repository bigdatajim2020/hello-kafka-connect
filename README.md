# hello-kafka-connect - example Kafka Connect source and sink

# ABOUT

[Kafka Connect](http://docs.confluent.io/2.0.0/connect/) is a secondary system on top of [Kafka](http://kafka.apache.org/) that simplifies common Kafka workflows, such as copying data between Kafka and databases, triggering actions on Kafka events, and supplying data feeds to Kafka.

# EXAMPLE

```
$ gradle clean shadowJar
...

$ docker-compose build --no-cache
...

$ docker-compose rm -f && docker-compose up --force-recreate
...

$ kafka-topics --zookeeper $(docker-machine ip default):2181/broker-0 --create --partitions 1 --replication-factor 1 --topic names
...

$ curl -XPOST $(docker-machine ip default):8083/connectors \
       -H "Content-Type: application/json" \
       -d "{
             \"name\": \"name-source\",
             \"config\": {
               \"connector.class\": \"us.yellosoft.hellokafkaconnect.NameSource\",
               \"tasks.max\": \"1\",
               \"topics\": \"names\",
               \"kafka_partitions\": \"1\",
               \"redis_address\": \"redis://$(docker-machine ip default):6379\",
               \"name_list_key\": \"names\"
              }
           }" | jq .

{
  "name": "name-source",
  "config": {
    "connector.class": "us.yellosoft.hellokafkaconnect.NameSource",
    "tasks.max": "1",
    "topics": "names",
    "kafka_partitions": "1",
    "redis_address": "redis://192.168.99.100:6379",
    "name_list_key": "names",
    "name": "name-source"
  },
  "tasks": []
}

$ curl $(docker-machine ip default):8083/connectors | jq .

[
  "name-source"
]

$ curl -XPOST $(docker-machine ip default):8083/connectors \
       -H "Content-Type: application/json" \
       -d "{
             \"name\": \"greeting-sink\",
             \"config\": {
               \"connector.class\": \"us.yellosoft.hellokafkaconnect.GreeterSink\",
               \"tasks.max\": \"1\",
               \"topics\": \"names\",
               \"redis_address\": \"redis://$(docker-machine ip default):6379\",
               \"greeting_list_key\": \"greetings\"
              }
           }" | jq .

{
  "name": "greeting-sink",
  "config": {
    "connector.class": "us.yellosoft.hellokafkaconnect.GreeterSink",
    "tasks.max": "1",
    "topics": "names",
    "redis_address": "redis://192.168.99.101:6379",
    "greeting_list_key": "greetings",
    "name": "greeting-sink"
  },
  "tasks": []
}

$ redis-cli -h $(docker-machine ip default) lpush names 'Alice'
(integer) 1

$ redis-cli -h $(docker-machine ip default) lpush names 'Bob'
(integer) 2

...

$ redis-cli -h $(docker-machine ip default) llen greetings
(integer) 0
```

The manual topic creation above is a workaround for auto-topic creation failing in kafka (connect). Generally, issues abound with Kafka Connect, and have been reported to both 1ambda/docker-kafka-connect and the confluent google group. Hopefully, someone can produce a working stack + configuration + connector code, because this little hello world example does NOT currently operate successfully.

# REQUIREMENTS

* [JDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html) 1.8+
* [Gradle](http://gradle.org/) 2.7+
* [ZooKeeper](https://zookeeper.apache.org/) 3+
* [Kafka](http://kafka.apache.org/) 0.10+
* [Kafka Connect](http://docs.confluent.io/2.0.0/connect/) 0.10+
* [Redis](http://redis.io/) 3+

## Optional

* [curl](https://curl.haxx.se/)
* [Docker Toolbox](https://www.docker.com/products/docker-toolbox)
* [jq](https://stedolan.github.io/jq/)
