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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.remoting.RemoteAccessException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.demo.DemoArchiver;
import ch.systemsx.cisd.openbis.dss.generic.shared.ArchiverTaskContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.IConfigProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetStatusUpdater;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProviderTestWrapper;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.generic.shared.dto.builders.DatasetDescriptionBuilder;

/**
 * @author Kaloyan Enimanev
 */
public class AbstractArchiverProcessingPluginTest extends AbstractFileSystemTestCase
{

    private static final String DATA_STORE_CODE = "DSS1";

    private IEncapsulatedOpenBISService service;

    private IConfigProvider configProvider;

    private Mockery context;

    @BeforeMethod
    public void beforeMethod()
    {
        context = new Mockery();

        final BeanFactory beanFactory = context.mock(BeanFactory.class);
        ServiceProviderTestWrapper.setApplicationContext(beanFactory);

        service = ServiceProviderTestWrapper.mock(context, IEncapsulatedOpenBISService.class);
        configProvider = ServiceProviderTestWrapper.mock(context, IConfigProvider.class);
        
        context.checking(new Expectations()
        {
            {
                    allowing(configProvider).getDataStoreCode();
                    will(returnValue(DATA_STORE_CODE));
            }
        });
        
    }

    @AfterMethod
    public void afterMethod()
    {
        context.assertIsSatisfied();
        ServiceProviderTestWrapper.restoreApplicationContext();
    }

    @Test
    public void testStatusRestoredIfRemoteCallFails()
    {
        AbstractArchiverProcessingPlugin archiver =
                new DemoArchiver(new Properties(), workingDirectory);
        final IDataSetStatusUpdater statusUpdater = context.mock(IDataSetStatusUpdater.class);
        archiver.setStatusUpdater(statusUpdater);

        DatasetDescriptionBuilder builder = new DatasetDescriptionBuilder("ds1");
        List<DatasetDescription> datasets = Arrays.asList(builder.getDatasetDescription());
        ArchiverTaskContext archiverContext = new ArchiverTaskContext(null);

        context.checking(new Expectations()
            {
                {
                    one(service).listDataSets();
                    will(throwException(new RemoteAccessException("service offline")));
                    
                    one(statusUpdater).update(Arrays.asList("ds1"), DataSetArchivingStatus.ARCHIVED , true);
                }
            });

        archiver.unarchive(datasets, archiverContext);

    }

}
