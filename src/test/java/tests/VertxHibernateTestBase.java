package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.hibernate.HibernateService;
import io.vertx.hibernate.utils.SimpleFuture;
import mock.model.Dog;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class VertxHibernateTestBase {
	
	protected JsonObject config;
	protected HibernateService service;
	protected Vertx vertx;
	
	@Before
	public void setUp(TestContext context) {
		Async async = context.async();
		vertx = Vertx.vertx();
		createConfig();
		service = new HibernateService(vertx, config);
		Future<Void> future = new SimpleFuture<Void>();
		future.setHandler(result -> {
			if (result.cause() != null)  {
				result.cause().printStackTrace();
			}
			assertTrue(result.succeeded());
			async.complete();
		});
		service.start(future);
	}
	
	@After
	public void tearDown(TestContext context) {
		if (service != null) {
			Async async = context.async();
			Future<Void> future = new SimpleFuture<Void>();
			future.setHandler(result -> {
				assertTrue(result.succeeded());
				async.complete();
			});
			service.stop(future);
		} 
	}
	
	@Test
	public void saveDog(TestContext context) {
		Async async= context.async();
		Dog snoop = new Dog("Snoopy", "Beagle");
		service.createSession(sessionHandler -> {
			if (sessionHandler.cause() != null) {
				sessionHandler.cause().printStackTrace();
			}
			assertTrue(sessionHandler.succeeded());
			String sessionId = sessionHandler.result();
			assertNotNull(sessionId);
			service.beginTransaction(sessionId, txHandler -> {
				assertTrue(txHandler.succeeded());
				service.persist(sessionId, snoop, saveHandler -> {
					assertTrue(saveHandler.succeeded());
					service.flushAndClose(sessionId, flushHandler -> {
						assertTrue(flushHandler.succeeded());
						service.createSession(secondSessionHandler -> {
							assertTrue(secondSessionHandler.succeeded());
							String secondSessionId = secondSessionHandler.result();
							assertNotNull(secondSessionId);
							service.find(secondSessionId, Dog.class, snoop.getName(), getHandler -> {
								assertTrue(getHandler.succeeded());
								assertTrue(snoop != getHandler.result());
								assertEquals(snoop, getHandler.result());
								async.complete();
							});
						});
					});
				});
			});
		});
	}
	
	private void createConfig() {
		this.config = new JsonObject();
		config.put("persistence-unit", "vertx-hibernate-tests");
	}
}
