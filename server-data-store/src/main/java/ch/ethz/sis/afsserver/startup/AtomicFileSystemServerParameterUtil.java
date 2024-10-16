package ch.ethz.sis.afsserver.startup;

import java.util.Arrays;
import java.util.List;

import ch.ethz.sis.afsjson.JsonObjectMapper;
import ch.ethz.sis.afsserver.server.common.OpenBISFacade;
import ch.ethz.sis.openbis.generic.OpenBIS;
import ch.ethz.sis.shared.io.IOUtils;
import ch.ethz.sis.shared.startup.Configuration;

public class AtomicFileSystemServerParameterUtil
{

    public static OpenBIS getOpenBIS(Configuration configuration)
    {
        return new OpenBIS(getOpenBISUrl(configuration), getOpenBISTimeout(configuration));
    }

    public static OpenBISFacade getOpenBISFacade(Configuration configuration)
    {
        return new OpenBISFacade(getOpenBISUrl(configuration), getOpenBISUser(configuration), getOpenBISPassword(configuration),
                getOpenBISTimeout(configuration));
    }

    public static String getOpenBISUrl(Configuration configuration)
    {
        return getStringParameter(configuration, AtomicFileSystemServerParameter.openBISUrl, true);
    }

    public static Integer getOpenBISTimeout(Configuration configuration)
    {
        return getIntegerParameter(configuration, AtomicFileSystemServerParameter.openBISTimeout, true);
    }

    public static String getOpenBISUser(Configuration configuration)
    {
        return getStringParameter(configuration, AtomicFileSystemServerParameter.openBISUser, true);
    }

    public static String getOpenBISPassword(Configuration configuration)
    {
        return getStringParameter(configuration, AtomicFileSystemServerParameter.openBISPassword, true);
    }

    public static String getOpenBISLastSeenDeletionFile(Configuration configuration)
    {
        return getStringParameter(configuration, AtomicFileSystemServerParameter.openBISLastSeenDeletionFile, true);
    }

    public static Integer getOpenBISLastSeenDeletionBatchSize(Configuration configuration)
    {
        return getIntegerParameter(configuration, AtomicFileSystemServerParameter.openBISLastSeenDeletionBatchSize, true);
    }

    public static Integer getOpenBISLastSeenDeletionIntervalInSeconds(Configuration configuration)
    {
        return getIntegerParameter(configuration, AtomicFileSystemServerParameter.openBISLastSeenDeletionIntervalInSeconds, true);
    }

    public static String getStorageRoot(Configuration configuration)
    {
        return getStringParameter(configuration, AtomicFileSystemServerParameter.storageRoot, true);
    }

    public static String getStorageUuid(Configuration configuration)
    {
        return getStringParameter(configuration, AtomicFileSystemServerParameter.storageUuid, true);
    }

    public static Integer getStorageIncomingShareId(Configuration configuration)
    {
        Integer storageIncomingShareId = getIntegerParameter(configuration, AtomicFileSystemServerParameter.storageIncomingShareId, false);
        String storageRoot = getStorageRoot(configuration);
        List<Integer> shares = Arrays.asList(IOUtils.getShares(storageRoot));

        if (storageIncomingShareId == null)
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

    public static JsonObjectMapper getJsonObjectMapper(Configuration configuration) throws Exception
    {
        getStringParameter(configuration, AtomicFileSystemServerParameter.jsonObjectMapperClass, true);
        return configuration.getInstance(AtomicFileSystemServerParameter.jsonObjectMapperClass);
    }

    public static String getInteractiveSessionKey(Configuration configuration)
    {
        return getStringParameter(configuration, AtomicFileSystemServerParameter.apiServerInteractiveSessionKey, true);
    }

    private static String getStringParameter(Configuration configuration, AtomicFileSystemServerParameter parameter, boolean mandatory)
    {
        String parameterValue = configuration.getStringProperty(parameter);

        if (parameterValue == null || parameterValue.isBlank())
        {
            if (mandatory)
            {
                throw new RuntimeException("Configuration parameter '" + parameter + "' cannot be null or empty.");
            } else
            {
                return null;
            }
        } else
        {
            return parameterValue;
        }
    }

    private static Integer getIntegerParameter(Configuration configuration, AtomicFileSystemServerParameter parameter, boolean mandatory)
    {
        String parameterStringValue = getStringParameter(configuration, parameter, mandatory);

        if (parameterStringValue != null)
        {
            try
            {
                return Integer.parseInt(parameterStringValue);
            } catch (NumberFormatException e)
            {
                throw new RuntimeException("Configuration parameter '" + parameter + "' is not a valid integer.");
            }
        } else
        {
            return null;
        }
    }

}
