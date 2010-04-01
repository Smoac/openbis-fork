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

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DatasetLocationUtil;
import ch.systemsx.cisd.openbis.dss.rpc.shared.FileInfoDss;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class DssServiceRpcV1Test extends AbstractFileSystemTestCase
{
    private Mockery context;

    private DssServiceRpcV1 rpcService;

    private IEncapsulatedOpenBISService openBisService;

    private static final String SESSION_TOKEN = "DummySessionToken";

    private static final String DATA_SET_CODE = "code";

    private static final String DB_INSTANCE_UUID = "UUID";

    @Override
    @BeforeMethod
    public void setUp() throws IOException
    {
        super.setUp();
        context = new Mockery();
        openBisService = context.mock(IEncapsulatedOpenBISService.class);
        rpcService = new DssServiceRpcV1(openBisService);
        rpcService.setStoreDirectory(workingDirectory);
        initializeDirectories();
    }

    private void initializeDirectories() throws IOException
    {
        File location =
                DatasetLocationUtil.getDatasetLocationPath(workingDirectory, DATA_SET_CODE,
                        DB_INSTANCE_UUID);
        location.mkdir();
    }

    @Test
    public void testDataSetListing()
    {
        final DatabaseInstance homeDatabaseInstance = new DatabaseInstance();
        homeDatabaseInstance.setCode("TEST");
        homeDatabaseInstance.setUuid(DB_INSTANCE_UUID);

        context.checking(new Expectations()
            {
                {
                    one(openBisService).checkDataSetAccess(SESSION_TOKEN, "code");
                    one(openBisService).getHomeDatabaseInstance();
                    will(returnValue(homeDatabaseInstance));
                }
            });
        FileInfoDss[] fileInfos = rpcService.listFilesForDataSet(SESSION_TOKEN, "code", "/", false);
        assertEquals(0, fileInfos.length);
    }

    @Test
    public void testInaccessibleDataSetListing()
    {

    }

    @Test
    public void testDataSetListingWithSneakyPath()
    {

    }
}
