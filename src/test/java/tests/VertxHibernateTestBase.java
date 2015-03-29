package tests;

import static org.junit.Assert.*;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.hibernate.HibernateService;
import mock.model.Dog;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class VertxHibernateTestBase {
	
	protected JsonObject config;
	protected HibernateService service;
	
	@Before
	public void setUp(TestContext context) {
		createConfig();
		createService();
	}
	
	@After
	public void tearDown(TestContext context) {
		if (service != null) {
			service.stop();
		}
	}
	
	@Test
	public void saveDog() {
		Dog snoop = new Dog("Snoopy", "Beagle");
		Session session = service.createSession();
		Transaction tx = session.beginTransaction();
		session.save(snoop);
		tx.commit();
		Session secondSession = service.createSession();
		assertNotNull(secondSession.get(Dog.class, snoop.getName()));
	}
	
	private void createConfig() {
		this.config = new JsonObject();
		config.put("dialect", "org.hibernate.dialect.H2Dialect");
		config.put("connection.driver_class", "org.h2.Driver");
		config.put("connection.url", "jdbc:h2:~/tests/h2");
		config.put("connection.username", "sa");
		config.put("show_sql", true);
		config.put("hbm2ddl.auto", "create");
		//config.put("default_schema", "test");
		JsonArray packages = new JsonArray("[\"mock.model\"]");
		config.put("packages", packages);
	}
	
	private void createService() {
		service = new HibernateService(config);
		service.start();
	}
}
