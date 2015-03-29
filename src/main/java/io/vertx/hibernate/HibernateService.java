package io.vertx.hibernate;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.Set;

import javax.persistence.Entity;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.reflections.Reflections;

public class HibernateService {
	
	private JsonObject config;
	private Configuration configuration;
	private SessionFactory sessionFactory;
	
	public HibernateService(JsonObject config) {
		this.config = config;
	}
	
	public void start() {
		System.out.println("----- Startup service");
		JsonArray packages = config.getJsonArray("packages");
		if (packages == null) {
			return;
		}
		
		configuration = new Configuration();
		configuration.setProperty("hibernate.dialect", config.getString("dialect"));
	    configuration.setProperty("hibernate.connection.driver_class", config.getString("connection.driver_class"));
	    configuration.setProperty("hibernate.connection.url", config.getString("connection.url"));
	    configuration.setProperty("hibernate.connection.username", config.getString("connection.username"));
	    if (config.getString("connection.password") != null) {
	    	configuration.setProperty("hibernate.connection.password", config.getString("connection.password"));
	    }
		if (config.getString("connection.datasource") != null)  {
			configuration.setProperty("hibernate.connection.datasource", config.getString("connection.datasource"));
		}
		if (config.getString("connection.pool_size") != null) {
		    configuration.setProperty("connection.pool_size", config.getString("connection.pool_size"));
		}
		if (config.getString("default_schema") != null) {
		    configuration.setProperty("hibernate.default_schema", config.getString("default_schema"));
		}
		if (config.getString("cache_provider_class") != null) {
		    configuration.setProperty("cache.provider_class", config.getString("cache_provider_class"));
		}
	    configuration.setProperty("show_sql", Boolean.toString(config.getBoolean("show_sql", false)));
	    configuration.setProperty("hibernate.hbm2ddl.auto", config.getString("hbm2ddl.auto", "update"));
		packages.forEach(aPackage -> {
			Reflections reflections = new Reflections((String)aPackage);
			Set<Class<?>> models = reflections.getTypesAnnotatedWith(Entity.class);
			models.forEach(model -> {
				configuration.addAnnotatedClass(model);
			});
		});
        ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
            .applySettings(configuration.getProperties()).build();
		sessionFactory = configuration.buildSessionFactory(serviceRegistry);
		System.out.println("----- Init done");
	}
	
	public Session createSession() {
		return sessionFactory.openSession();
	}
	
	public void stop() {
	}
}
