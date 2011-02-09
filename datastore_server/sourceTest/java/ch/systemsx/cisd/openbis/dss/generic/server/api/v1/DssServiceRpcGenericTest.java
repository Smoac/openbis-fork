/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.api.v1;

import java.io.File;
import java.util.Arrays;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.etlserver.Constants;
import ch.systemsx.cisd.openbis.dss.generic.server.DssServiceRpcAuthorizationAdvisor;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.IDssServiceRpcGeneric;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DatasetLocationUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.DataSetBuilder;


/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class DssServiceRpcGenericTest extends AbstractFileSystemTestCase
{
    private static final String DB_UUID = "db-uuid";

    private static final String SESSION_TOKEN = "SESSION";
    
    private IEncapsulatedOpenBISService service;
    private Mockery context;
    private IDssServiceRpcGeneric dssService;
    private File store;

    @BeforeMethod
    public void beforeMethod()
    {
        context = new Mockery();
        service = context.mock(IEncapsulatedOpenBISService.class);
        ProxyFactoryBean proxyFactoryBean = new ProxyFactoryBean();
        proxyFactoryBean.setInterfaces(new Class[] {IDssServiceRpcGeneric.class});
        DssServiceRpcGeneric nakedDssService = new DssServiceRpcGeneric(service);
        proxyFactoryBean.setTarget(nakedDssService);
        proxyFactoryBean.addAdvisor(new DssServiceRpcAuthorizationAdvisor());
        dssService = (IDssServiceRpcGeneric) proxyFactoryBean.getObject();
        context.checking(new Expectations()
            {
                {
                    allowing(service).getHomeDatabaseInstance();
                    DatabaseInstance databaseInstance = new DatabaseInstance();
                    databaseInstance.setCode("db-code");
                    databaseInstance.setUuid(DB_UUID);
                    will(returnValue(databaseInstance));
                }
            });
        store = new File(workingDirectory, "store");
        store.mkdirs();
        nakedDssService.setStoreDirectory(store);
    }
    
    @AfterMethod
    public void tearDown()
    {
        context.assertIsSatisfied();
    }

    @Test
    public void testFilesForData() 
    {
        final String dataSetCode = "ds-1";
        prepareCheckDataSetAccess(dataSetCode);
        prepareCheckDataSetAccess(dataSetCode);
        prepareListDataSetsByCode(dataSetCode);
        File location =
                DatasetLocationUtil.getDatasetLocationPath(store, dataSetCode,
                        ch.systemsx.cisd.openbis.dss.generic.shared.Constants.DEFAULT_SHARE_ID, DB_UUID);
        location.mkdirs();
        
        FileInfoDssDTO[] dataSets = dssService.listFilesForDataSet(SESSION_TOKEN, dataSetCode, "abc/de", true);
        
        assertEquals("FileInfoDssDTO[/abc/de,0]", dataSets[0].toString());
        assertEquals(1, dataSets.length);
        context.assertIsSatisfied();
    }

    private void prepareListDataSetsByCode(final String dataSetCode)
    {
        context.checking(new Expectations()
            {
                {
                    one(service).listDataSetsByCode(Arrays.asList(dataSetCode));
                    will(returnValue(Arrays.asList(new DataSetBuilder().code(dataSetCode)
                            .shareId(ch.systemsx.cisd.openbis.dss.generic.shared.Constants.DEFAULT_SHARE_ID).getDataSet())));
                }
            });
    }

    private void prepareCheckDataSetAccess(final String dataSetCode)
    {
        context.checking(new Expectations()
            {
                {
                    one(service).checkDataSetAccess(SESSION_TOKEN, dataSetCode);
                }
            });
    }
}
