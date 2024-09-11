package ch.ethz.sis.afsserver.server.shuffling;

import ch.ethz.sis.afsserver.startup.AtomicFileSystemServerParameterUtil;
import ch.ethz.sis.shared.startup.Configuration;

public class ServiceProvider
{
    private static volatile boolean initialized = false;

    private static Configuration configuration;

    private static IShareIdManager shareIdManager;

    private static EncapsulatedOpenBISService openBISService;

    private static IConfigProvider configProvider;

    public static void configure(Configuration configuration)
    {
        ServiceProvider.configuration = configuration;
    }

    public static IShareIdManager getShareIdManager()
    {
        initialize();
        return shareIdManager;
    }

    public static EncapsulatedOpenBISService getOpenBISService()
    {
        initialize();
        return openBISService;
    }

    public static IConfigProvider getConfigProvider()
    {
        initialize();
        return configProvider;
    }

    private static void initialize()
    {
        // initialize lazily only to verify configuration properties if they are really needed

        if (!initialized)
        {
            synchronized (ServiceProvider.class)
            {
                if (!initialized)
                {
                    if (configuration == null)
                    {
                        throw new RuntimeException("Cannot initialize with null configuration");
                    }

                    EncapsulatedOpenBISService encapsulatedOpenBISService =
                            new EncapsulatedOpenBISService(AtomicFileSystemServerParameterUtil.getOpenBISFacade(configuration));
                    ShareIdManager shareIdManager =
                            new ShareIdManager(encapsulatedOpenBISService,
                                    AtomicFileSystemServerParameterUtil.getDataSetLockingTimeout(configuration));
                    ConfigProvider configProvider = new ConfigProvider(configuration);

                    ServiceProvider.openBISService = encapsulatedOpenBISService;
                    ServiceProvider.shareIdManager = shareIdManager;
                    ServiceProvider.configProvider = configProvider;
                    initialized = true;
                }
            }
        }
    }

}
