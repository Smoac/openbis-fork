package ch.ethz.sis.afsserver.server.observer;

import ch.ethz.sis.afsserver.server.ApiServer;
import ch.ethz.sis.shared.startup.Configuration;
public interface ServerObserver<CONNECTION> {

    public void init(ApiServer<CONNECTION, ?, ?, ?> apiServer, Configuration configuration) throws Exception;

    public void beforeStartup() throws Exception;

    public void beforeShutdown() throws Exception;
}
