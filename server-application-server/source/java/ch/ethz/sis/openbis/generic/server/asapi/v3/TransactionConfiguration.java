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

    private static final String LOG_FOLDER_PATH_PROPERTY_NAME = "api.v3.two-phase-commit.transaction-log-folder-path";

    private static final String LOG_FOLDER_PATH_DEFAULT = "two-phase-commit-log";

    private static final String TRANSACTION_COUNT_LIMIT_PROPERTY_NAME = "api.v3.two-phase-commit.transaction-count-limit";

    private static final int TRANSACTION_COUNT_LIMIT_DEFAULT = 10;

    private static final String DATASTORE_SERVER_URL_PROPERTY_NAME = "api.v3.two-phase-commit.datastore-server.url";

    private static final String DATASTORE_SERVER_TIMEOUT_PROPERTY_NAME = "api.v3.two-phase-commit.datastore-server.timeout";

    private static final int DATASTORE_SERVER_TIMEOUT_DEFAULT = 300;

    @Resource(name = ExposablePropertyPlaceholderConfigurer.PROPERTY_CONFIGURER_BEAN_NAME)
    private ExposablePropertyPlaceholderConfigurer configurer;

    private String coordinatorKey;

    private String interactiveSessionKey;

    private String transactionLogFolderPath;

    private int transactionCountLimit;

    private String dataStoreServerUrl;

    private int dataStoreServerTimeoutInSeconds;

    @PostConstruct
    private void init()
    {
        Properties properties = configurer.getResolvedProps();
        coordinatorKey = PropertyUtils.getProperty(properties, COORDINATOR_KEY_PROPERTY_NAME, generateRandomKey());
        interactiveSessionKey = PropertyUtils.getProperty(properties, INTERACTIVE_SESSION_KEY_PROPERTY_NAME, generateRandomKey());
        transactionLogFolderPath = PropertyUtils.getProperty(properties, LOG_FOLDER_PATH_PROPERTY_NAME, LOG_FOLDER_PATH_DEFAULT);
        transactionCountLimit = PropertyUtils.getInt(properties, TRANSACTION_COUNT_LIMIT_PROPERTY_NAME, TRANSACTION_COUNT_LIMIT_DEFAULT);
        dataStoreServerUrl = PropertyUtils.getMandatoryProperty(properties, DATASTORE_SERVER_URL_PROPERTY_NAME);
        dataStoreServerTimeoutInSeconds = PropertyUtils.getInt(properties, DATASTORE_SERVER_TIMEOUT_PROPERTY_NAME, DATASTORE_SERVER_TIMEOUT_DEFAULT);
    }

    public String getCoordinatorKey()
    {
        return coordinatorKey;
    }

    public String getInteractiveSessionKey()
    {
        return interactiveSessionKey;
    }

    public String getTransactionLogFolderPath()
    {
        return transactionLogFolderPath;
    }

    public int getTransactionCountLimit()
    {
        return transactionCountLimit;
    }

    public String getDataStoreServerUrl()
    {
        return dataStoreServerUrl;
    }

    public int getDataStoreServerTimeoutInSeconds()
    {
        return dataStoreServerTimeoutInSeconds;
    }

    private String generateRandomKey()
    {
        return RandomStringUtils.random(16, 0, 0, true, true, null, new SecureRandom());
    }
}
