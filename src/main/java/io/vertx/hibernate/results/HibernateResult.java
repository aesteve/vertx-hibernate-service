package io.vertx.hibernate.results;

public class HibernateResult<T> {
	private Throwable failure;
	private T result;
	
	public boolean failed() {
		return failure != null; 
	}
	
	public Throwable cause() {
		return failure;
	}
	
	public void setResult(T result) {
		this.result = result;
	}
	
	public T result() {
		return result;
	}
}
