/*
 * Copyright 2010 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.collections.IKeyExtractor;
import ch.systemsx.cisd.common.collections.TableMap;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.spring.AbstractServiceWithLogger;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DatasetLocationUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * Abstract superclass of DssServiceRpc implementations.
 * <p>
 * Provides methods to check security and access to data sets.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public abstract class AbstractDssServiceRpc<T> extends AbstractServiceWithLogger<T>
{
    static protected final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            AbstractDssServiceRpc.class);

    private final IEncapsulatedOpenBISService openBISService;

    private File storeDirectory;

    private DatabaseInstance homeDatabaseInstance;

    /**
     * Configuration method to set the path to the DSS store. Should only be called by the object
     * that configures the RPC services.
     */
    public void setStoreDirectory(File aFile)
    {
        storeDirectory = aFile;
    }

    /**
     * Configuration method to set the directory for incoming data sets. Should only be called by
     * the object that configures the RPC services.
     */
    public void setIncomingDirectory(File aFile)
    {
        // For subclasses to override
    }

    /**
     * Constructor with required reference to the openBIS service.
     * 
     * @param openBISService
     */
    protected AbstractDssServiceRpc(IEncapsulatedOpenBISService openBISService)
    {
        this.openBISService = openBISService;
    }

    protected IEncapsulatedOpenBISService getOpenBISService()
    {
        return openBISService;
    }

    /**
     * Get a file representing the root of the DSS store.
     */
    protected File getStoreDirectory()
    {
        return storeDirectory;
    }

    /**
     * Get the home database instance for the openBIS instance I connect to.
     */
    protected DatabaseInstance getHomeDatabaseInstance()
    {
        // Not synchronized because it doesn't cause any harm if the ivar is initialized twice.
        if (homeDatabaseInstance == null)
        {
            homeDatabaseInstance = openBISService.getHomeDatabaseInstance();
        }
        return homeDatabaseInstance;
    }

    /**
     * Asserts that specified data set is accessible by the user of the specified session.
     */
    protected void assertDatasetIsAccessible(String sessionToken, String dataSetCode)
    {
        if (isDatasetAccessible(sessionToken, dataSetCode) == false)
        {
            throw new IllegalArgumentException("User is not allowed to access data set "
                    + dataSetCode);
        }
    }

    /**
     * Check with openBIS if the user with the given sessionToken is allowed to access the data set
     * specified by the dataSetCode.
     */
    public boolean isDatasetAccessible(String sessionToken, String dataSetCode)
    {
        boolean access;
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("Check access to the data set '%s' on openBIS server.",
                    dataSetCode));
        }

        try
        {
            openBISService.checkDataSetAccess(sessionToken, dataSetCode);
            access = true;
        } catch (UserFailureException ex)
        {
            access = false;
        }

        return access;
    }

    public boolean isSpaceWriteable(String sessionToken, SpaceIdentifier spaceId)
    {
        boolean access;
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("Check access to the space '%s' on openBIS server.",
                    spaceId));
        }

        try
        {
            openBISService.checkSpaceAccess(sessionToken, spaceId);
            access = true;
        } catch (UserFailureException ex)
        {
            access = false;
        }

        return access;
    }

    /**
     * Asserts that specified data sets are all accessible by the user of the specified session.
     */
    protected void checkDatasetsAuthorization(String sessionToken, Set<String> dataSetCodes)
    {
        if (isSessionAuthorizedForDatasets(sessionToken, dataSetCodes) == false)
        {
            throw new IllegalArgumentException(
                    "User is not allowed to access at least one of the following data sets: "
                            + dataSetCodes);
        }
    }

    /**
     * Check with openBIS and return a collection of the data sets the user with the given
     * sessionToken is allowed to access.
     * 
     * @param sessionToken The session token for the user.
     * @param dataSetCodes The data set codes we want to check access for.
     * @return True if all the data sets are accessible, false if one or more are not accessible.
     */
    protected boolean isSessionAuthorizedForDatasets(String sessionToken, Set<String> dataSetCodes)
    {
        boolean access;
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format(
                    "Check access to the data sets '%s' on openBIS server.", dataSetCodes));
        }

        try
        {
            openBISService.checkDataSetCollectionAccess(sessionToken, new ArrayList<String>(dataSetCodes));
            access = true;
        } catch (UserFailureException ex)
        {
            access = false;
        }

        return access;
    }
    
    protected File getRootDirectory(String datasetCode)
    {
        List<ExternalData> list = getOpenBISService().listDataSetsByCode(Arrays.asList(datasetCode));
        if (list.isEmpty())
        {
            throw new IllegalArgumentException("Unknown data set " + datasetCode);
        }
        return getRootDirectoryForDataSet(datasetCode, list.get(0).getShareId());
    }

    /**
     * Get the top level of the folder for the data set.
     */
    protected File getRootDirectoryForDataSet(String code, String shareId)
    {
        File dataSetRootDirectory =
                DatasetLocationUtil.getDatasetLocationPath(getStoreDirectory(), code, shareId,
                        getHomeDatabaseInstance().getUuid());
        return dataSetRootDirectory;
    }

    protected File checkAccessAndGetRootDirectory(String sessionToken, String dataSetCode)
            throws IllegalArgumentException
    {
        if (isDatasetAccessible(sessionToken, dataSetCode) == false)
        {
            throw new IllegalArgumentException("Path does not exist.");
        }
        return getRootDirectory(dataSetCode);
    }

    /**
     * Return a map keyed by data set code with value root directory for that data set.
     */
    protected Map<String, File> checkAccessAndGetRootDirectories(String sessionToken,
            Set<String> dataSetCodes) throws IllegalArgumentException
    {
        if (isSessionAuthorizedForDatasets(sessionToken, dataSetCodes) == false)
        {
            throw new IllegalArgumentException("Path does not exist.");
        }

        HashMap<String, File> rootDirectories = new HashMap<String, File>();
        List<ExternalData> dataSets =
                openBISService.listDataSetsByCode(new ArrayList<String>(dataSetCodes));
        TableMap<String, ExternalData> tableMap =
                new TableMap<String, ExternalData>(dataSets,
                        new IKeyExtractor<String, ExternalData>()
                            {
                                public String getKey(ExternalData e)
                                {
                                    return e.getCode();
                                }
                            });
        for (String datasetCode : dataSetCodes)
        {
            ExternalData dataSet = tableMap.tryGet(datasetCode);
            if (dataSet == null)
            {
                throw new IllegalArgumentException("Unknown data set " + datasetCode);
            }
            rootDirectories.put(datasetCode, getRootDirectory(datasetCode, dataSet.getShareId()));
        }
        return rootDirectories;
    }

    private File getRootDirectory(String dataSetCode, String shareId)
    {
        File dataSetRootDirectory = getRootDirectoryForDataSet(dataSetCode, shareId);
        if (dataSetRootDirectory.exists() == false)
        {
            throw new IllegalArgumentException("Path does not exist: " + dataSetRootDirectory);
        }
        return dataSetRootDirectory;
    }

    protected File checkAccessAndGetFile(String sessionToken, String dataSetCode, String path)
            throws IOException, IllegalArgumentException
    {
        File dataSetRootDirectory = checkAccessAndGetRootDirectory(sessionToken, dataSetCode);
        return getDatasetFile(path, dataSetRootDirectory);
    }

    private static File getDatasetFile(String path, File dataSetRootDirectory) throws IOException
    {
        String dataSetRootPath = dataSetRootDirectory.getCanonicalPath();
        File requestedFile = new File(dataSetRootDirectory, path);
        // Make sure the requested file is under the root of the data set
        if (requestedFile.getCanonicalPath().startsWith(dataSetRootPath) == false)
        {
            throw new IllegalArgumentException("Path does not exist.");
        }
        return requestedFile;
    }

    protected ExternalData tryGetDataSet(String sessionToken, String dataSetCode)
    {
        return openBISService.tryGetDataSet(sessionToken, dataSetCode);
    }
}