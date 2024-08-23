package ch.ethz.sis.afsserver.server.shuffling;

public class ServiceProvider
{

    private static IShareIdManager shareIdManager;

    private static EncapsulatedOpenBISService openBISService;

    public static IShareIdManager getShareIdManager()
    {
        // TODO: temporary implementation

        if (shareIdManager == null)
        {
            synchronized (ServiceProvider.class)
            {
                if (shareIdManager == null)
                {
                    shareIdManager = new ShareIdManager(getOpenBISService(), 84600);
                }
            }
        }

        return shareIdManager;
    }

    public static EncapsulatedOpenBISService getOpenBISService()
    {
        // TODO: temporary implementation

        if (openBISService == null)
        {
            synchronized (ServiceProvider.class)
            {
                if (openBISService == null)
                {
                    openBISService = new EncapsulatedOpenBISService();
                }
            }
        }

        return openBISService;
    }
}
