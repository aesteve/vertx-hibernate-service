package io.vertx.hibernate.results;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

public class ListAndCount<T> {

	private EntityManager em;
	private List<T> result;
	private Long count;
	private Class<T> type;
	
	public ListAndCount(Class<T> type, EntityManager em) {
		this.em = em;
		this.type = type;
	}
	
	@SuppressWarnings("unchecked")
	public void queryAndCount(Integer first, Integer last) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<T> crit = builder.createQuery(type);
		crit.from(type);
		Query query = em.createQuery(crit);
		if (first != null) {
			query.setFirstResult(first);
			if (last != null) {
				query.setMaxResults(last - first);
			}
		}
		result = query.getResultList();
		CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
		countQuery.select(builder.count(countQuery.from(type)));
		query = em.createQuery(countQuery);
		count = (Long)query.getSingleResult();
	}
	
	public List<T> result() {
		return result;
	}
	
	public Long count() {
		return count;
	}
}
