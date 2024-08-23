package ch.ethz.sis.afsserver.server.shuffling;

public class ServiceProvider
{

    private static IShareIdManager shareIdManager;

    private static EncapsulatedOpenBISService openBISService;

    public static IShareIdManager getShareIdManager()
    {
        return shareIdManager;
    }

    public static void setShareIdManager(final IShareIdManager shareIdManager)
    {
        ServiceProvider.shareIdManager = shareIdManager;
    }

    public static EncapsulatedOpenBISService getOpenBISService()
    {
        return openBISService;
    }

    public static void setOpenBISService(final EncapsulatedOpenBISService openBISService)
    {
        ServiceProvider.openBISService = openBISService;
    }
}
