package io.vertx.hibernate.async;

import io.vertx.core.AsyncResult;

public class HibernateAsyncResult<T> implements AsyncResult<T> {


	private final Throwable cause;
	private final T result;

	public HibernateAsyncResult(String cause) {
		this(new RuntimeException(cause), null);
	}

	public HibernateAsyncResult(Throwable cause) {
		this(cause, null);
	}

	public HibernateAsyncResult(Throwable cause, T result) {
		this.cause = cause;
		this.result = result;
	}

	@Override
	public T result() {
		return result;
	}

	@Override
	public Throwable cause() {
		return cause;
	}

	@Override
	public boolean succeeded() {
		return cause == null;
	}

	@Override
	public boolean failed() {
		return cause != null;
	}

}
