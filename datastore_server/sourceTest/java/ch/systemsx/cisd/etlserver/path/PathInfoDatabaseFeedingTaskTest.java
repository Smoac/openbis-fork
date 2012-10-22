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

package ch.systemsx.cisd.etlserver.path;

import static ch.systemsx.cisd.common.action.IDelegatedAction.DO_NOTHING;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.IHierarchicalContentFactory;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.DataSetBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;

/**
 * @author Franz-Josef Elmer
 */
@Friend(toClasses = PathInfoDatabaseFeedingTask.class)
public class PathInfoDatabaseFeedingTaskTest extends AbstractFileSystemTestCase
{
    private static final String DATA_SET_CODE = "ds1";

    private Mockery context;

    private IEncapsulatedOpenBISService service;

    private IDataSetDirectoryProvider directoryProvider;

    private IShareIdManager shareIdManager;

    private IPathsInfoDAO dao;

    private IHierarchicalContentFactory contentFactory;

    private IHierarchicalContentNode node;

    private IHierarchicalContent content;

    private PathInfoDatabaseFeedingTask task;

    private File dataSetFolder;

    @BeforeMethod
    public void beforeMethod()
    {
        LogInitializer.init();
        context = new Mockery();
        service = context.mock(IEncapsulatedOpenBISService.class);
        directoryProvider = context.mock(IDataSetDirectoryProvider.class);
        shareIdManager = context.mock(IShareIdManager.class);
        context.checking(new Expectations()
            {
                {
                    allowing(directoryProvider).getShareIdManager();
                    will(returnValue(shareIdManager));
                }
            });
        dao = context.mock(IPathsInfoDAO.class);
        contentFactory = context.mock(IHierarchicalContentFactory.class);
        content = context.mock(IHierarchicalContent.class);
        node = context.mock(IHierarchicalContentNode.class);
        task = new PathInfoDatabaseFeedingTask(service, directoryProvider, dao, contentFactory, true);
        dataSetFolder = new File(workingDirectory, "ds1");
        dataSetFolder.mkdirs();
    }

    @AfterMethod
    public void tearDown(Method method)
    {
        try
        {
            context.assertIsSatisfied();
        } catch (Throwable t)
        {
            // assert expectations were met, including the name of the failed method
            throw new Error(method.getName() + "() : ", t);
        }
    }

    @Test
    public void testAsMaintenanceTask()
    {
        final SimpleDataSetInformationDTO ds1 = new SimpleDataSetInformationDTO();
        ds1.setDataSetCode("ds1");
        ds1.setDataSetLocation("abc1");
        final SimpleDataSetInformationDTO ds2 = new SimpleDataSetInformationDTO();
        ds2.setDataSetCode("ds2");
        ds2.setDataSetLocation("abc2");
        context.checking(new Expectations()
            {
                {
                    one(service).listDataSets();
                    will(returnValue(Arrays.asList(ds1, ds2)));
                }
            });
        prepareHappyCase(ds1);
        prepareFailing(ds2);

        task.execute();
    }

    @Test
    public void testPostRegistrationHappyCase()
    {
        final DataSet dataSet =
                new DataSetBuilder().code(DATA_SET_CODE).location("abc").getDataSet();
        context.checking(new Expectations()
            {
                {
                    one(service).tryGetDataSet(DATA_SET_CODE);
                    will(returnValue(dataSet));

                }
            });
        prepareHappyCase(dataSet);

        task.createExecutor(DATA_SET_CODE, false).execute();
    }

    @Test
    public void testPostRegistrationFailingCase()
    {
        final DataSet dataSet =
                new DataSetBuilder().code(DATA_SET_CODE).location("abc").getDataSet();
        context.checking(new Expectations()
            {
                {
                    one(service).tryGetDataSet(DATA_SET_CODE);
                    will(returnValue(dataSet));
                }
            });
        prepareFailing(dataSet);

        task.createExecutor(DATA_SET_CODE, false).execute();
    }

    @Test
    public void testNonExistingDataSetFolder()
    {
        context.checking(new Expectations()
            {
                {
                    one(service).tryGetDataSet(DATA_SET_CODE);
                    DataSet dataSet =
                            new DataSetBuilder().code(DATA_SET_CODE).location("abc").getDataSet();
                    will(returnValue(dataSet));

                    one(shareIdManager).lock(DATA_SET_CODE);

                    one(directoryProvider).getDataSetDirectory(dataSet);
                    will(returnValue(new File(workingDirectory, "blabla")));

                    exactly(2).of(shareIdManager).releaseLocks();
                }
            });

        task.createExecutor(DATA_SET_CODE, false).execute();
    }

    @Test
    public void testAlreadyExistingDataSetInDatabaser()
    {
        context.checking(new Expectations()
            {
                {
                    one(service).tryGetDataSet(DATA_SET_CODE);
                    DataSet dataSet =
                            new DataSetBuilder().code(DATA_SET_CODE).location("abc").getDataSet();
                    will(returnValue(dataSet));

                    one(shareIdManager).lock(DATA_SET_CODE);

                    one(directoryProvider).getDataSetDirectory(dataSet);
                    will(returnValue(dataSetFolder));

                    one(dao).tryGetDataSetId(dataSet.getDataSetCode());
                    will(returnValue(42L));

                    one(shareIdManager).releaseLocks();
                }
            });

        task.createExecutor(DATA_SET_CODE, false).execute();
    }

    private void prepareHappyCase(final IDatasetLocation dataSet)
    {
        context.checking(new Expectations()
            {
                {
                    one(shareIdManager).lock(dataSet.getDataSetCode());

                    one(directoryProvider).getDataSetDirectory(dataSet);
                    will(returnValue(dataSetFolder));

                    one(dao).tryGetDataSetId(dataSet.getDataSetCode());
                    will(returnValue(null));

                    one(dao).createDataSet(dataSet.getDataSetCode(), dataSet.getDataSetLocation());
                    will(returnValue(101L));

                    one(contentFactory).asHierarchicalContent(dataSetFolder, DO_NOTHING);
                    will(returnValue(content));

                    one(contentFactory).asHierarchicalContentNode(content, dataSetFolder);
                    will(returnValue(node));

                    one(node).exists();
                    will(returnValue(true));

                    one(node).getName();
                    will(returnValue("ds1-root"));

                    one(node).getFileLength();
                    will(returnValue(12345L));

                    one(node).getChecksumCRC32();
                    will(returnValue(789));
                    
                    one(node).isDirectory();
                    will(returnValue(false));

                    one(node).getLastModified();
                    will(returnValue(42L));

                    one(dao).createDataSetFiles(
                            with(equal(Collections.singletonList(new PathEntryDTO(101L, null,
                                    "", "ds1-root", 12345L, 789, false, new Date(42))))));

                    one(dao).commit();
                    one(shareIdManager).releaseLocks();
                }
            });
    }

    private void prepareFailing(final IDatasetLocation dataSet)
    {
        context.checking(new Expectations()
            {
                {
                    one(shareIdManager).lock(dataSet.getDataSetCode());

                    one(directoryProvider).getDataSetDirectory(dataSet);
                    will(returnValue(dataSetFolder));

                    one(dao).tryGetDataSetId(dataSet.getDataSetCode());
                    will(returnValue(null));

                    one(dao).createDataSet(dataSet.getDataSetCode(), dataSet.getDataSetLocation());
                    will(returnValue(101L));

                    one(contentFactory).asHierarchicalContent(dataSetFolder, DO_NOTHING);
                    will(throwException(new RuntimeException("Oophs!")));

                    one(dao).rollback();
                    one(shareIdManager).releaseLocks();
                }
            });
    }
}
