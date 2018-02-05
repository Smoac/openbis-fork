package ch.ethz.sis.microservices.server.logging;

public class LogFactoryFactory {
    public LogFactory create(String logFactoryClass) throws Exception {
        return (LogFactory) Class.forName(logFactoryClass).newInstance();
    }
}
