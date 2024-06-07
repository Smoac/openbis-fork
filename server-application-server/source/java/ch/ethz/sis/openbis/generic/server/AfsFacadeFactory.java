package ch.ethz.sis.openbis.generic.server;

import java.net.URI;
import java.util.Properties;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import ch.ethz.sis.afsclient.client.AfsClient;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.spring.ExposablePropertyPlaceholderConfigurer;

@Component
public class AfsFacadeFactory
{

    private static final String AFS_SERVER_URL_PROPERTY_NAME = "afs-server.url";

    private static final String AFS_SERVER_TIMEOUT_PROPERTY_NAME = "afs-server.timeout";

    private static final String AFS_SERVER_INTERACTIVE_SESSION_KEY_PROPERTY_NAME = "afs-server.interactive-session-key";

    @Resource(name = ExposablePropertyPlaceholderConfigurer.PROPERTY_CONFIGURER_BEAN_NAME)
    private ExposablePropertyPlaceholderConfigurer configurer;

    private String afsServerUrl;

    private int afsServerTimeoutInSeconds;

    private String afsServerInteractiveSessionKey;

    private boolean initialized;

    public AfsFacade createAfsFacade(String sessionToken)
    {
        init();
        AfsClient afsClient = new AfsClient(URI.create(afsServerUrl), afsServerTimeoutInSeconds * 1000);
        afsClient.setSessionToken(sessionToken);
        afsClient.setInteractiveSessionKey(afsServerInteractiveSessionKey);
        return new AfsFacade(afsClient);
    }

    private synchronized void init()
    {
        if (initialized)
        {
            return;
        }

        Properties properties = configurer.getResolvedProps();
        afsServerUrl = PropertyUtils.getMandatoryProperty(properties, AFS_SERVER_URL_PROPERTY_NAME);

        String timeoutString = PropertyUtils.getMandatoryProperty(properties, AFS_SERVER_TIMEOUT_PROPERTY_NAME);
        try
        {
            afsServerTimeoutInSeconds = Integer.parseInt(timeoutString);
        } catch (NumberFormatException e)
        {
            throw new ConfigurationFailureException("'" + AFS_SERVER_TIMEOUT_PROPERTY_NAME + "' configuration property is set to '" + timeoutString
                    + "' value which is not a valid number");
        }

        afsServerInteractiveSessionKey = PropertyUtils.getMandatoryProperty(properties, AFS_SERVER_INTERACTIVE_SESSION_KEY_PROPERTY_NAME);

        initialized = true;
    }

}
