FROM debezium/connect:0.2
MAINTAINER Andrew Pennebaker <andrew.pennebaker@gmail.com>
COPY build/libs/hello-kafka-connect-all.jar $KAFKA_HOME/connectors/
