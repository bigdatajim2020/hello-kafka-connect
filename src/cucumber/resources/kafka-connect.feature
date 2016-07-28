Feature: Kafka Connect integration
  Scenario: Hello World kafka connect
    Given redis is running at URI "redis://192.168.99.100:6379"
    When a supplier generates names onto redis list "names"
    Then connectors welcome the names onto redis list "greetings"
