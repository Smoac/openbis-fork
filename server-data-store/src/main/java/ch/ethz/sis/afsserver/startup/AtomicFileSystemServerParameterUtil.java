package ch.ethz.sis.afsserver.startup;

import java.util.Arrays;
import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.shared.io.IOUtils;
import ch.ethz.sis.shared.startup.Configuration;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;

public class AtomicFileSystemServerParameterUtil
{

    public static IApplicationServerApi getApplicationServerApi(Configuration configuration)
    {
        String openBISUrl = configuration.getStringProperty(AtomicFileSystemServerParameter.openBISUrl);

        if (openBISUrl == null || openBISUrl.isBlank())
        {
            throw new RuntimeException("Configuration parameter '" + AtomicFileSystemServerParameter.openBISUrl + "' cannot be null or empty.");
        }

        String openBISTimeout = configuration.getStringProperty(AtomicFileSystemServerParameter.openBISTimeout);

        if (openBISTimeout == null || openBISTimeout.isBlank())
        {
            throw new RuntimeException("Configuration parameter '" + AtomicFileSystemServerParameter.openBISTimeout + "' cannot be null or empty.");
        }

        int openBISTimeoutInt;

        try
        {
            openBISTimeoutInt = Integer.parseInt(openBISTimeout);
        } catch (NumberFormatException e)
        {
            throw new RuntimeException("Configuration parameter '" + AtomicFileSystemServerParameter.openBISTimeout + "' is not a valid integer.");
        }

        return HttpInvokerUtils.createServiceStub(IApplicationServerApi.class, openBISUrl, openBISTimeoutInt);
    }

    public static String getStorageRoot(Configuration configuration)
    {
        String storageRoot = configuration.getStringProperty(AtomicFileSystemServerParameter.storageRoot);

        if (storageRoot == null || storageRoot.isBlank())
        {
            throw new RuntimeException("Configuration parameter '" + AtomicFileSystemServerParameter.storageRoot + "' cannot be null or empty.");
        }

        return storageRoot;
    }

    public static String getStorageUuid(Configuration configuration)
    {
        String storageUuid = configuration.getStringProperty(AtomicFileSystemServerParameter.storageUuid);

        if (storageUuid == null || storageUuid.isBlank())
        {
            throw new RuntimeException("Configuration parameter '" + AtomicFileSystemServerParameter.storageUuid + "' cannot be null or empty.");
        }

        return storageUuid;
    }

    public static String getStoreIncomingShareId(Configuration configuration)
    {
        String storageIncomingShareId = configuration.getStringProperty(AtomicFileSystemServerParameter.storageIncomingShareId);
        String storageRoot = getStorageRoot(configuration);
        List<String> shares = Arrays.asList(IOUtils.getShares(storageRoot));

        if (storageIncomingShareId == null || storageIncomingShareId.isBlank())
        {
            if (shares.isEmpty())
            {
                throw new RuntimeException(
                        "No shares were found in the storage root '" + storageRoot + "' defined in '" + AtomicFileSystemServerParameter.storageRoot
                                + "' configuration parameter.");
            } else
            {
                storageIncomingShareId = shares.get(0);
            }
        } else if (!shares.contains(storageIncomingShareId))
        {
            throw new RuntimeException("Share '" + storageIncomingShareId + "' defined in '" + AtomicFileSystemServerParameter.storageIncomingShareId
                    + "' configuration parameter does not exist in the storage root '" + storageRoot + "'.");
        }

        return storageIncomingShareId;
    }

}
