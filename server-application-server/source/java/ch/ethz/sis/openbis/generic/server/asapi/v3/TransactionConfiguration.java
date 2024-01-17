package ch.ethz.sis.openbis.generic.server.asapi.v3;

import java.security.SecureRandom;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;

import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.spring.ExposablePropertyPlaceholderConfigurer;

@Component
public class TransactionConfiguration
{

    private static final String COORDINATOR_KEY_PROPERTY_NAME = "api.v3.two-phase-commit.coordinator-key";

    private static final String INTERACTIVE_SESSION_KEY_PROPERTY_NAME = "api.v3.two-phase-commit.interactive-session-key";

    private static final String LOG_FOLDER_PATH_PROPERTY_NAME = "api.v3.two-phase-commit.log-folder-path";

    private static final String LOG_FOLDER_PATH_DEFAULT_VALUE = "api.v3.two-phase-commit-log";

    @Resource(name = ExposablePropertyPlaceholderConfigurer.PROPERTY_CONFIGURER_BEAN_NAME)
    private ExposablePropertyPlaceholderConfigurer configurer;

    private String coordinatorKey;

    private String interactiveSessionKey;

    private String logFolderPath;

    @PostConstruct
    private void init(){
        Properties properties = configurer.getResolvedProps();
        coordinatorKey = PropertyUtils.getProperty(properties, COORDINATOR_KEY_PROPERTY_NAME, generateRandomKey());
        interactiveSessionKey = PropertyUtils.getProperty(properties, INTERACTIVE_SESSION_KEY_PROPERTY_NAME, generateRandomKey());
        logFolderPath = PropertyUtils.getProperty(properties, LOG_FOLDER_PATH_PROPERTY_NAME, LOG_FOLDER_PATH_DEFAULT_VALUE);
    }

    public String getCoordinatorKey()
    {
        return coordinatorKey;
    }

    public String getInteractiveSessionKey()
    {
        return interactiveSessionKey;
    }

    public String getLogFolderPath()
    {
        return logFolderPath;
    }

    private String generateRandomKey(){
        return RandomStringUtils.random(16, 0, 0, true, true, null, new SecureRandom());
    }
}
