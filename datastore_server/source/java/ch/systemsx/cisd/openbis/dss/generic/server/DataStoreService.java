/*
 * Copyright 2009 ETH Zuerich, CISD
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
import java.util.List;

import org.springframework.beans.factory.InitializingBean;

import ch.systemsx.cisd.common.exceptions.InvalidAuthenticationException;
import ch.systemsx.cisd.common.exceptions.WrappedIOException;
import ch.systemsx.cisd.common.filesystem.QueueingPathRemoverService;
import ch.systemsx.cisd.common.spring.AbstractServiceWithLogger;
import ch.systemsx.cisd.openbis.generic.shared.IDataStoreService;

/**
 * Implementation of {@link IDataStoreService} which will be accessed remotely by the opneBIS
 * server.
 * 
 * @author Franz-Josef Elmer
 */
public class DataStoreService extends AbstractServiceWithLogger<IDataStoreService> implements
        IDataStoreService, InitializingBean
{
    private final SessionTokenManager sessionTokenManager;
    
    private File storeRoot;

    public DataStoreService(SessionTokenManager sessionTokenManager)
    {
        this.sessionTokenManager = sessionTokenManager;
    }
    
    public final void setStoreRoot(File storeRoot)
    {
        this.storeRoot = storeRoot;
    }

    public void afterPropertiesSet() throws Exception
    {
        String prefix = "Property 'storeRoot' ";
        if (storeRoot == null)
        {
            throw new IllegalStateException(prefix + "not set.");
        }
        String storeRootPath = storeRoot.getAbsolutePath();
        if (storeRoot.isFile())
        {
            throw new IllegalArgumentException(prefix + "is a file instead of a directory: "
                    + storeRootPath);
        }
        if (storeRoot.exists() == false)
        {
            if (storeRoot.mkdirs() == false)
            {
                throw new WrappedIOException(new IOException(
                        "Couldn't create root directory of the data store: " + storeRootPath));
            }
            if (operationLog.isInfoEnabled())
            {
                operationLog.info("Creates root directory of the data store: " + storeRootPath);
            }
        }
    }

    @Override
    protected Class<IDataStoreService> getProxyInterface()
    {
        return IDataStoreService.class;
    }

    public IDataStoreService createLogger(boolean invocationSuccessful)
    {
        return new DataStoreServiceLogger(operationLog, invocationSuccessful);
    }

    public int getVersion(String sessionToken)
    {
        sessionTokenManager.assertValidSessionToken(sessionToken);
        
        return IDataStoreService.VERSION;
    }

    public void deleteDataSets(String sessionToken, List<String> dataSetLocations)
            throws InvalidAuthenticationException
    {
        sessionTokenManager.assertValidSessionToken(sessionToken);
        
        for (String location : dataSetLocations)
        {
            QueueingPathRemoverService.removeRecursively(new File(storeRoot, location));
        }
    }

}
