# hello-kafka-connect - example Kafka Connect source and sink

# ABOUT

[Kafka Connect](http://docs.confluent.io/2.0.0/connect/) is a secondary system on top of [Kafka](http://kafka.apache.org/) that simplifies common Kafka workflows, such as copying data between Kafka and databases, triggering actions on Kafka events, and supplying data feeds to Kafka.

hello-kafka-connect is a demonstration of how to develop and deploy source and sink connectors to a Kafka Connect cluster, using Redis as an example of an external system that can integrate with Kafka.

# EXAMPLE

We've done most of the work with Docker Compose, but there is a small workaround required to account for lack of official Confluent entry on DockerHub:

```
$ git clone https://github.com/confluentinc/docker-images
$ cd docker-images/kafka/
$ docker build -t confluent/kafka .
...

$ cd ../../hello-kafka-connect/
```

Compile `src/main/...` Redis example connectors and tasks, and pack into a JAR:

```
$ gradle clean shadowJar
...
```

Add JAR to Kafka Connect container:

```
$ docker-compose build --no-cache
...
```

The Kafka and Kafka Connect nodes require advertised hostname configuration. Due to Docker kernel requirements, advertised hostnames may not simply match localhost AKA 127.0.0.1 on non-Linux system; Instead, advertised hostnames must  match the Docker Machine IP address.

Mac and Windows users can configure docker-compose with:

```
$ ln -sf docker-compose-docker-machine.yml.sample docker-compose.yml
```

Linux users can:

```
$ ln -sf docker-compose-linux-host.yml.sample docker-compose.yml
```

Launch the full Kafka Connect stack:

```
$ docker-compose rm -f && docker-compose up --force-recreate
...
```

Submit connectors to the Kafka cluster:

```
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

$ curl $(docker-machine ip default):8083/connectors | jq .

[
  "greeting-sink",
  "name-source"
]
```

Finally, generate sample data to trigger data flowing through the system: a Redis list -> source connector -> `names` Kafka topic -> sink connector -> another Redis list.

```
$ redis-cli -h $(docker-machine ip default) lpush names 'Alice'
(integer) 1

$ redis-cli -h $(docker-machine ip default) lpush names 'Bob'
(integer) 2

$ redis-cli -h $(docker-machine ip default) lpop greetings
"Welcome, Bob"

$ redis-cli -h $(docker-machine ip default) lpop greetings
"Welcome, Alice"
```

# REQUIREMENTS

* [JDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html) 1.8+
* [Gradle](http://gradle.org/) 2.7+
* [ZooKeeper](https://zookeeper.apache.org/) 3+
* [Kafka](http://kafka.apache.org/) 0.10+
* [Kafka Connect](http://docs.confluent.io/3.0.0/connect/) 0.10+
* [Redis](http://redis.io/) 3+

## Optional

* [curl](https://curl.haxx.se/)
* [Docker Toolbox](https://www.docker.com/products/docker-toolbox)
* [jq](https://stedolan.github.io/jq/)
