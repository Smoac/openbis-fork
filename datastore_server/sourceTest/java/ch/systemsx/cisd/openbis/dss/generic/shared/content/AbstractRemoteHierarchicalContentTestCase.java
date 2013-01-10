/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.shared.content;

import java.io.File;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.IFileOperations;
import ch.systemsx.cisd.openbis.dss.generic.shared.ISingleDataSetPathInfoProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.IDssServiceRpcGeneric;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatasetLocation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocation;
import ch.systemsx.cisd.openbis.generic.shared.dto.OpenBISSessionHolder;

/**
 * @author Franz-Josef Elmer
 */
public abstract class AbstractRemoteHierarchicalContentTestCase extends AbstractFileSystemTestCase
{
    protected static final String DATA_STORE_URL = "http://a.b.c";

    protected static final String SESSION_TOKEN = "token";
    
    private static final String DATA_STORE_CODE = "DSS";
    
    protected static final String DATA_SET_CODE = "DS-123";
    
    protected static final IDatasetLocation DATA_SET_LOCATION = new DatasetLocation(
            DATA_SET_CODE, "a/b/c", DATA_STORE_CODE, DATA_STORE_URL);
    
    protected static final String STARTED_MESSAGE = "started";

    protected static final String FINISHED_MESSAGE = "finished";

    protected static final String FILE1_CONTENT = "hello file one";

    protected static final String FILE2_CONTENT = "hello file two";

    protected Mockery context;

    protected IFileOperations fileOperations;

    protected IDssServiceRpcGenericFactory serviceFactory;

    protected IDssServiceRpcGeneric remoteDss;

    protected ISingleDataSetPathInfoProvider pathInfoProvider;

    protected OpenBISSessionHolder sessionHolder;
    
    protected File workSpace;

    protected File remoteFile1;
    
    protected File remoteFile2;
    
    @BeforeMethod
    public void setUpBasicFixture()
    {
        context = new Mockery();
        fileOperations = context.mock(IFileOperations.class);
        serviceFactory = context.mock(IDssServiceRpcGenericFactory.class);
        remoteDss = context.mock(IDssServiceRpcGeneric.class, "remote DSS");
        pathInfoProvider = context.mock(ISingleDataSetPathInfoProvider.class);
        workSpace = new File(workingDirectory, "workspace");
        sessionHolder = new OpenBISSessionHolder();
        sessionHolder.setSessionToken(SESSION_TOKEN);
        context.checking(new Expectations()
        {
            {
                allowing(serviceFactory).getService(DATA_STORE_URL);
                will(returnValue(remoteDss));
            }
        });
        File remoteStore = new File(workingDirectory, "remote-store");
        File remoteDataSetFolder = new File(remoteStore, DATA_SET_CODE);
        remoteDataSetFolder.mkdirs();
        remoteFile1 = new File(remoteDataSetFolder, "file1.txt");
        FileUtilities.writeToFile(remoteFile1, FILE1_CONTENT);
        remoteFile2 = new File(remoteDataSetFolder, "file2.txt");
        FileUtilities.writeToFile(remoteFile2, FILE2_CONTENT);
    }
    
    @AfterMethod
    public void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    protected ContentCache createCache(boolean sessionCache)
    {
        if (sessionCache == false)
        {
            context.checking(new Expectations()
                {
                    {
                        one(fileOperations).removeRecursivelyQueueing(
                                new File(workSpace, ContentCache.DOWNLOADING_FOLDER));
                    }
                });
        }
        return new ContentCache(serviceFactory, workSpace, sessionCache, fileOperations);
    }


}
