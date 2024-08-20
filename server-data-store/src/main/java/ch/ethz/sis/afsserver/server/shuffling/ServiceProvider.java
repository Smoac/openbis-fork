package ch.ethz.sis.afsserver.server.shuffling;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;

public class ServiceProvider
{
    public static IShareIdManager getShareIdManager()
    {
        return null;
    }

    public static IConfigProvider getConfigProvider()
    {
        return null;
    }

    public static IApplicationServerApi getV3ApplicationService()
    {
        return null;
    }

    public static IEncapsulatedOpenBISService getOpenBISService()
    {
        return null;
    }
}
