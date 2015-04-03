package tests;

import static org.junit.Assert.assertTrue;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.hibernate.HibernateService;
import io.vertx.hibernate.utils.SimpleFuture;

import org.junit.After;
import org.junit.Before;

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
	
	private void createConfig() {
		this.config = new JsonObject();
		config.put("persistence-unit", "vertx-hibernate-tests");
	}
}
