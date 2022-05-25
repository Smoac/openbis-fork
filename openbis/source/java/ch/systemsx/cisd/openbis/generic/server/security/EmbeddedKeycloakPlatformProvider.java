package ch.systemsx.cisd.openbis.generic.server.security;

import java.io.File;

import org.apache.log4j.Logger;
import org.keycloak.platform.PlatformProvider;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

public class EmbeddedKeycloakPlatformProvider implements PlatformProvider
{

    private static final Logger log = LogFactory.getLogger(LogCategory.OPERATION, EmbeddedKeycloakPlatformProvider.class);

    private Runnable shutdownHook;

    @Override
    public void onStartup(Runnable startupHook)
    {
        startupHook.run();
    }

    @Override
    public void onShutdown(Runnable shutdownHook)
    {
        this.shutdownHook = shutdownHook;
    }

    @Override
    public void exit(Throwable cause)
    {
        log.fatal(cause);
        log.error("Exiting because of error", cause);
        exit(1);
    }

    @Override
    public File getTmpDirectory()
    {
        return new File(System.getProperty("java.io.tmpdir"));
    }

    private void exit(int status)
    {
        new Thread(() -> System.exit(status)).start();
    }

}
