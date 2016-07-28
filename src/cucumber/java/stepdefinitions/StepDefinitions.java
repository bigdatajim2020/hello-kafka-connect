package stepdefinitions;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;
import cucumber.api.java.en.Then;

import io.inbot.testfixtures.RandomNameGenerator;
import redis.clients.jedis.Jedis;

import org.junit.Assert;

import java.util.List;
import java.util.LinkedList;

public class StepDefinitions {
  public static final int WAIT = 3000; // ms
  public static final int COUNT = 100;

  private RandomNameGenerator randomNameGenerator;
  private Jedis jedis;
  private List<String> names = new LinkedList<>();

  public StepDefinitions() {
    this.randomNameGenerator = new RandomNameGenerator(System.currentTimeMillis());
  }

  @Given("^redis is running at URI \"(.+)\"$")
  public void redisIsRunningAtURI(String redisURI) {
    this.jedis = new Jedis(redisURI);
    this.jedis.connect();
  }

  @When("^a supplier generates names onto redis list \"(.+)\"$")
  public void aSupplierGeneratesNamesOntoRedisList(String redisList) {
    for (int i = 0; i < COUNT; i++) {
      final String name = randomNameGenerator.nextFullName();

      names.add(name);
      jedis.lpush(redisList, name);
    }
  }

  @Then("^connectors welcome the names onto redis list \"(.+)\"$")
  public void connectorsWelcomeTheNamesOntoRedisList(String redisList) {
    try {
      Thread.sleep(WAIT);
    } catch (InterruptedException e) {}

    long greetingCount = jedis.llen(redisList);

    Assert.assertEquals(names.size(), greetingCount);

    jedis.del(redisList);
  }
}
