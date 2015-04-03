package tests.crud;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import mock.model.Dog;

import org.junit.Test;
import org.junit.runner.RunWith;

import tests.VertxHibernateTestBase;

@RunWith(VertxUnitRunner.class)
public class SingleOperation extends VertxHibernateTestBase {
	
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
}
