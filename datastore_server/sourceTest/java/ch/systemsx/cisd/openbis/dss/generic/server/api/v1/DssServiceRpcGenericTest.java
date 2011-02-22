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

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.openbis.dss.generic.server.DatasetSessionAuthorizer;
import ch.systemsx.cisd.openbis.dss.generic.server.DssServiceRpcAuthorizationAdvisor;
import ch.systemsx.cisd.openbis.dss.generic.shared.Constants;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.authorization.internal.DssSessionAuthorizationHolder;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.IDssServiceRpcGeneric;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DatasetLocationUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;


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

    private IShareIdManager shareIdManager;

    @BeforeMethod
    public void beforeMethod()
    {
        DssSessionAuthorizationHolder.setAuthorizer(new DatasetSessionAuthorizer());
        final StaticListableBeanFactory applicationContext = new StaticListableBeanFactory();
        ServiceProvider.setBeanFactory(applicationContext);
        context = new Mockery();
        service = context.mock(IEncapsulatedOpenBISService.class);
        shareIdManager = context.mock(IShareIdManager.class);
        applicationContext.addBean("openBIS-service", service);
        ProxyFactoryBean proxyFactoryBean = new ProxyFactoryBean();
        proxyFactoryBean.setInterfaces(new Class[] {IDssServiceRpcGeneric.class});
        DssServiceRpcGeneric nakedDssService = new DssServiceRpcGeneric(service, shareIdManager);
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
        prepareGetShareId(dataSetCode);
        File location =
                DatasetLocationUtil.getDatasetLocationPath(store, dataSetCode,
                        Constants.DEFAULT_SHARE_ID, DB_UUID);
        location.mkdirs();

        FileInfoDssDTO[] dataSets =
                dssService.listFilesForDataSet(SESSION_TOKEN, dataSetCode, "abc/de", true);

        assertEquals("FileInfoDssDTO[/abc/de,0]", dataSets[0].toString());
        assertEquals(1, dataSets.length);
        context.assertIsSatisfied();
    }

    private void prepareGetShareId(final String dataSetCode)
    {
        context.checking(new Expectations()
            {
                {
                    one(shareIdManager).getShareId(dataSetCode);
                    will(returnValue(Constants.DEFAULT_SHARE_ID));
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
