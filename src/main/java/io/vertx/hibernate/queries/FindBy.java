package io.vertx.hibernate.queries;

import java.util.Collection;

import javax.persistence.EntityManager;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.hibernate.jpa.HibernateEntityManager;

public class FindBy<T, P> {
	private HibernateEntityManager em;
	private Class<? extends T> clazz;
	
	public FindBy(Class<? extends T> clazz, EntityManager em) {
		this.em = em.unwrap(HibernateEntityManager.class);
		this.clazz = clazz;
	}
	
	@SuppressWarnings("unchecked")
	public T find(String name, P propertyValue) {
		Session session = em.getSession();
		Criteria crit = session.createCriteria(clazz);
		crit.add(Restrictions.eq(name, propertyValue));
		return (T)crit.uniqueResult();
	}
	
	@SuppressWarnings("unchecked")
	public Collection<T> findAll(String name, P propertyValue) {
		Session session = em.getSession();
		Criteria crit = session.createCriteria(clazz);
		crit.add(Restrictions.eq(name, propertyValue));
		return (Collection<T>)crit.list();
	}	
}
