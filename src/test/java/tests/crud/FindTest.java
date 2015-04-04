package tests.crud;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import io.vertx.core.Future;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.hibernate.queries.FindBy;
import mock.model.Dog;

import org.junit.Test;
import org.junit.runner.RunWith;

import tests.VertxHibernateTestBase;

@RunWith(VertxUnitRunner.class)
public class FindTest extends VertxHibernateTestBase {

	@Test
	public void findByName(TestContext context) {
		Dog snoop = new Dog("Snoopy", "Beagle");
		Async async = context.async();
		Future<Void> future = Future.future();
		
		future.setHandler(handler -> {
			service.withEntityManager(em -> {
				FindBy<Dog, String> fb = new FindBy<Dog, String>(Dog.class, em);
				return fb.find("name", snoop.getName());
			}, res2 -> {
				assertEquals(snoop, res2.result());
				async.complete();
			});
		});
		
		service.withinTransaction(em -> {
			em.persist(snoop);
			return snoop;
		}, res -> {
			if (res.failed()) {
				res.cause().printStackTrace();
			}
			assertTrue(res.succeeded());
			future.complete();
		});
	}
}
