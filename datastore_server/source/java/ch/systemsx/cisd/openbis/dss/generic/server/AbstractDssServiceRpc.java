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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.io.IHierarchicalContent;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.spring.AbstractServiceWithLogger;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DatasetLocationUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;

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

    private IHierarchicalContentProvider contentProvider;

    private IShareIdManager shareIdManager;

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
    protected AbstractDssServiceRpc(IEncapsulatedOpenBISService openBISService,
            IShareIdManager shareIdManager, IHierarchicalContentProvider contentProvider)
    {
        this.openBISService = openBISService;
        this.shareIdManager = shareIdManager;
        this.contentProvider = contentProvider;
    }

    protected IHierarchicalContentProvider getHierarchicalContentProvider()
    {
        if (contentProvider == null)
        {
            contentProvider = ServiceProvider.getHierarchicalContentProvider();
        }
        return contentProvider;
    }

    protected IShareIdManager getShareIdManager()
    {
        if (shareIdManager == null)
        {
            shareIdManager = ServiceProvider.getShareIdManager();
        }
        return shareIdManager;
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

    protected IHierarchicalContent getHierarchicalContent(String dataSetCode)
    {
        return getHierarchicalContentProvider().asContent(dataSetCode);
    }

    @Deprecated
    protected File getRootDirectory(String datasetCode)
    {
        return getRootDirectoryForDataSet(datasetCode, getShareIdManager().getShareId(datasetCode));
    }

    /**
     * Get the top level of the folder for the data set.
     */
    @Deprecated
    private File getRootDirectoryForDataSet(String code, String shareId)
    {
        File dataSetRootDirectory =
                DatasetLocationUtil.getDatasetLocationPath(getStoreDirectory(), code, shareId,
                        getHomeDatabaseInstance().getUuid());
        return dataSetRootDirectory;
    }

    /**
     * Return a map keyed by data set code with value root directory for that data set.
     */
    protected Map<String, File> getRootDirectories(String sessionToken, Set<String> dataSetCodes)
            throws IllegalArgumentException
    {
        HashMap<String, File> rootDirectories = new HashMap<String, File>();
        for (String datasetCode : dataSetCodes)
        {
            String shareId = getShareIdManager().getShareId(datasetCode);
            File dataSetRootDirectory = getRootDirectoryForDataSet(datasetCode, shareId);
            if (dataSetRootDirectory.exists() == false)
            {
                throw new IllegalArgumentException("Path does not exist: " + dataSetRootDirectory);
            }
            File rootDirectory = dataSetRootDirectory;
            rootDirectories.put(datasetCode, rootDirectory);
        }
        return rootDirectories;
    }

    protected File getDatasetFile(String dataSetCode, String path) throws IOException
    {
        File dataSetRootDirectory = getRootDirectory(dataSetCode);
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