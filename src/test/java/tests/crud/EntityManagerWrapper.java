package tests.crud;

import static org.junit.Assert.*;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import javax.persistence.EntityTransaction;

import mock.model.Dog;

import org.junit.Test;
import org.junit.runner.RunWith;

import tests.VertxHibernateTestBase;

@RunWith(VertxUnitRunner.class)
public class EntityManagerWrapper extends VertxHibernateTestBase {
	
	@Test
	public void wrapEntityManagerAndSave(TestContext context) {
		Async async = context.async();
		final Dog bill = new Dog("Bill", "Cocker");
		service.executeWithEntityManager(entityManager -> {
			EntityTransaction tx = entityManager.getTransaction();
			tx.begin();
			entityManager.persist(bill);
			tx.commit();
			return null;
		}, result -> {
			if (result.cause() != null) {
				result.cause().printStackTrace();
			}
			assertTrue(result.succeeded());
			service.executeWithEntityManager(secondEm -> {
				Dog dog = secondEm.find(Dog.class, "Bill");
				return dog;
			}, secondResult -> {
				assertTrue(secondResult.succeeded());
				assertEquals(secondResult.result(), bill);
				async.complete();
			});
		});
	}
}
