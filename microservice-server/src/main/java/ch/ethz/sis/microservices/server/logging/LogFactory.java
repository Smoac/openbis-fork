package ch.ethz.sis.microservices.server.logging;

public interface LogFactory {
    <T> Logger getLogger(Class<T> clazz);
}