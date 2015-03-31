package io.vertx.hibernate;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.hibernate.async.HibernateAsyncResult;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

public class HibernateService {
	
	private JsonObject config;
	private EntityManagerFactory entityManagerFactory;
	private Vertx vertx;
	private Random rand;
	private Map<String, EntityManager> managers;
	
	public HibernateService(Vertx vertx, JsonObject config) {
		this.config = config;
		this.vertx = vertx;
		this.rand = new Random();
		managers = new HashMap<String, EntityManager>();
	}
	
	public void start(Future<Void> startFuture) {
		System.out.println("----- Startup service");
		String persistenceUnit = config.getString("persistence-unit");
		if (persistenceUnit == null) {
			startFuture.fail("No persistence-unit specified in config");
			return;
		}
        vertx.executeBlocking(future -> {
        	try {
        		entityManagerFactory = Persistence.createEntityManagerFactory(persistenceUnit);
        		future.complete();
        	} catch(Exception e) {
        		future.fail(e);
        	}
        }, res -> {
        	if (res.succeeded()) {
        		System.out.println("----- Init done");
            	startFuture.complete();
        	} else {
        		System.out.println("----- Init failed");
            	startFuture.fail(res.cause());
        	}
        });
	}
	
	public void createSession(Handler<AsyncResult<String>> handler) {
		vertx.executeBlocking(future -> {
			try {
				String sessionId = generateSessionId();
				EntityManager em = entityManagerFactory.createEntityManager();
				getMap().put(sessionId, em);
				future.complete(sessionId);
			} catch(Exception e) {
				future.fail(e);
			}
		}, res -> {
			AsyncResult<String> async;
			if (res.succeeded()) {
				async = new HibernateAsyncResult<String>(null, res.result().toString());
			} else {
				async = new HibernateAsyncResult<String>(res.cause(), null);
			}
			handler.handle(async);
		});
	}

	public void beginTransaction(String sessionId, Handler<AsyncResult<Void>> handler) {
		EntityManager manager = getManager(sessionId);
		if (manager == null) {
			handler.handle(new HibernateAsyncResult<Void>("No entity manager found with id : "+sessionId));
			return;
		} 
		vertx.executeBlocking(future -> {
			try {
				manager.getTransaction().begin();
				future.complete();
			} catch(Exception he) {
				future.fail(he);
			}
		}, res -> {
			AsyncResult<Void> async;
			if (res.succeeded()) {
				async = new HibernateAsyncResult<Void>(null, null);
			} else {
				async = new HibernateAsyncResult<Void>(res.cause(), null);
			}
			handler.handle(async);
		});
	}
	
	public void flushSession(String sessionId, Handler<AsyncResult<Void>> handler) {
		EntityManager entityManager = getManager(sessionId);
		if (entityManager == null) {
			handler.handle(new HibernateAsyncResult<Void>("No entity manager found with id : "+sessionId));
			return;
		} 
		vertx.executeBlocking(future -> {
			try {
				entityManager.flush();
				future.complete();
			} catch(Exception e) {
				future.fail(e);
			}
		}, res -> {
			AsyncResult<Void> async;
			if (res.succeeded()) {
				async = new HibernateAsyncResult<Void>(null, null);
			} else {
				async = new HibernateAsyncResult<Void>(res.cause(), null);
			}
			handler.handle(async);
		});
	}
	
	public void clearSession(String sessionId, Handler<AsyncResult<Void>> handler) {
		EntityManager entityManager = getManager(sessionId);
		if (entityManager == null) {
			handler.handle(new HibernateAsyncResult<Void>("No entity manager found with id : "+sessionId));
			return;
		} 
		vertx.executeBlocking(future -> {
			try {
				entityManager.clear();
				future.complete();
			} catch(Exception e) {
				future.fail(e);
			}
		}, res -> {
			AsyncResult<Void> async;
			if (res.succeeded()) {
				async = new HibernateAsyncResult<Void>(null, null);
			} else {
				async = new HibernateAsyncResult<Void>(res.cause(), null);
			}
			handler.handle(async);
		});
	}
	
	public void flushAndClose(String sessionId, Handler<AsyncResult<Void>> handler) {
		EntityManager entityManager = getManager(sessionId);
		if (entityManager == null) {
			handler.handle(new HibernateAsyncResult<Void>("No entity manager found with id : "+sessionId));
			return;
		} 
		vertx.executeBlocking(future -> {
			try {
				getMap().remove(sessionId);
				entityManager.flush();
				if (entityManager.getTransaction() != null) {
					entityManager.getTransaction().commit();
				}
				entityManager.close();
				future.complete();
			} catch(Exception e) {
				future.fail(e);
			}
		}, res -> {
			AsyncResult<Void> async;
			if (res.succeeded()) {
				async = new HibernateAsyncResult<Void>(null, null);
			} else {
				async = new HibernateAsyncResult<Void>(res.cause(), null);
			}
			handler.handle(async);
		});
	}
	
	public void saveWithinTransaction(String sessionId, Object model, Handler<AsyncResult<Void>> handler) {
		EntityManager entityManager = getManager(sessionId);
		if (entityManager == null) {
			handler.handle(new HibernateAsyncResult<Void>("No session found with id : "+sessionId));
			return;
		} 
		vertx.executeBlocking(future -> {
			try {
				EntityTransaction tx = entityManager.getTransaction();
				tx.begin();
				entityManager.persist(model);
				tx.commit();
				future.complete();
			} catch (Exception e) {
				future.fail(e);
			}
		}, res -> {
			if (res.succeeded()) {
				handler.handle(new HibernateAsyncResult<Void>(null, null));
			} else {
				handler.handle(new HibernateAsyncResult<Void>(res.cause(), null));
			}
		});
	}
	
	public void find(String sessionId, Class<?> clazz, Serializable id, Handler<AsyncResult<Object>> handler) {
		EntityManager entityManager = getManager(sessionId);
		if (entityManager == null) {
			handler.handle(new HibernateAsyncResult<Object>("No entity manager found with id : "+sessionId));
			return;
		} 
		vertx.executeBlocking(future -> {
			try {
				Object result = entityManager.find(clazz, id);
				future.complete(result);
			} catch(Exception e) {
				future.fail(e);
			}
		}, res -> {
			AsyncResult<Object> async;
			if (res.succeeded()) {
				async = new HibernateAsyncResult<Object>(null, res.result());
			} else {
				async = new HibernateAsyncResult<Object>(res.cause(), null);
			}
			handler.handle(async);
		});		
	}
	
	public CriteriaBuilder getCriteriaBuilder(String sessionId) {
		EntityManager entityManager = getManager(sessionId);
		if (entityManager == null) {
			return null;
		}
		return entityManager.getCriteriaBuilder();
	}
	
	public void persist(String sessionId, Object model, Handler<AsyncResult<Void>> handler) {
		EntityManager entityManager = getManager(sessionId);
		if (entityManager == null) {
			handler.handle(new HibernateAsyncResult<Void>("No entity manager found with id : "+sessionId));
			return;
		} 
		vertx.executeBlocking(future -> {
			try {
				entityManager.persist(model);
				future.complete();
			} catch (Exception e) {
				future.fail(e);
			}
		}, res -> {
			if (res.succeeded()) {
				handler.handle(new HibernateAsyncResult<Void>(null, null)) ;
			} else {
				handler.handle(new HibernateAsyncResult<Void>(res.cause(), null));
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	public<T> void list(String sessionId, CriteriaQuery<T> criteria, Integer firstItem, Integer lastItem, Handler<AsyncResult<List<T>>> handler) {
		EntityManager entityManager = getManager(sessionId);
		if (entityManager == null) {
			handler.handle(new HibernateAsyncResult<List<T>>("No entity manager found with id : "+sessionId));
			return;
		} 
		vertx.executeBlocking(future -> {
			try {
				Query query = entityManager.createQuery(criteria);
				if (firstItem != null) {
					query.setFirstResult(firstItem);
					if (lastItem != null) {
						query.setMaxResults(lastItem - firstItem);
					}
				}
				List<T> list = query.getResultList();
				future.complete(list);
			} catch (Exception e) {
				future.fail(e);
			}
		}, res -> {
			if (res.succeeded()) {
				handler.handle(new HibernateAsyncResult<List<T>>(null, (List<T>)res.result()));
			} else {
				handler.handle(new HibernateAsyncResult<List<T>>(res.cause(), null));
			}
		});		
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public<T> void singleResult(String sessionId, CriteriaQuery<T> criteria, Handler<AsyncResult<T>> handler) {
		EntityManager entityManager = getManager(sessionId);
		if (entityManager == null) {
			handler.handle(new HibernateAsyncResult<T>("No entity manager found with id : "+sessionId));
			return;
		} 
		vertx.executeBlocking(future -> {
			try {
				Query query = entityManager.createQuery(criteria);
				future.complete(query.getSingleResult());
			} catch (Exception e) {
				future.fail(e);
			}
		}, res -> {
			if (res.succeeded()) {
				handler.handle(new HibernateAsyncResult(null, res.result()));
			} else {
				handler.handle(new HibernateAsyncResult(res.cause(), null));
			}
		});			
	}
	
	// TODO : update, delete, merge, persist, createQuery, ...
	
	
	public void stop(Future<Void> future) {
		future.complete();
	}
	
	private String generateSessionId() {
		return "HibernateSession-" + System.currentTimeMillis() + "-" + rand.nextInt();
	}
	
	private EntityManager getManager(String sessionId) {
		return getMap().get(sessionId);
	}
	
	private Map<String, EntityManager> getMap() {
		return managers;
	}
}
