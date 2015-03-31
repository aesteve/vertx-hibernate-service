# vertx-hibernate-service
Hibernate service for Vert.x

------

This project aims at providing every javax.persistence utilities through Hibernate as a Vert.x microservice you could invoke either programmatically or through Vert.x's event bus.

It's wrapping fundamental primitives of the `EntityManagerFactory` and `EntityManager` in a non-blocking way.

You call the service API or send a message over the event bus, then wait for an async answer without blocking Vert.x's event loop.

-------

Work in progress.
