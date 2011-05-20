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

package ch.systemsx.cisd.openbis.dss.generic.shared;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.io.IHierarchicalContent;
import ch.systemsx.cisd.common.io.IHierarchicalContentFactory;
import ch.systemsx.cisd.common.io.IHierarchicalContentNode;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.common.utilities.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ContainerDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocation;

/**
 * Test of {@link HierarchicalContentProvider}
 * 
 * @author Piotr Buczek
 */
public class HierarchicalContentProviderTest extends AssertJUnit
{

    private IDataSetDirectoryProvider directoryProvider;

    private IHierarchicalContentFactory hierarchicalContentFactory;

    private IEncapsulatedOpenBISService openbisService;

    private IShareIdManager shareIdManager;

    private Mockery context;

    private IHierarchicalContentProvider hierarchicalContentProvider;

    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        shareIdManager = context.mock(IShareIdManager.class);
        directoryProvider = context.mock(IDataSetDirectoryProvider.class);
        hierarchicalContentFactory = context.mock(IHierarchicalContentFactory.class);
        context.checking(new Expectations()
            {
                {
                    allowing(directoryProvider).getShareIdManager();
                    will(returnValue(shareIdManager));
                }
            });
        openbisService = context.mock(IEncapsulatedOpenBISService.class);
        hierarchicalContentProvider =
                new HierarchicalContentProvider(openbisService, directoryProvider,
                        hierarchicalContentFactory);
    }

    @AfterMethod
    public void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    void testAsContentFromDataSetCode()
    {
        final String dataSetCode = "DS_CODE";

        final DataSet data = new DataSet();
        data.setCode(dataSetCode);
        final File dataRootFile = new File("DS_FILE");
        final RecordingMatcher<IDelegatedAction> actionMatcher = RecordingMatcher.create();
        context.checking(new Expectations()
            {
                {

                    one(openbisService).tryGetDataSet(dataSetCode);
                    will(returnValue(data));

                    one(shareIdManager).lock(dataSetCode);
                    one(directoryProvider).getDataSetDirectory(data);
                    will(returnValue(dataRootFile));

                    one(hierarchicalContentFactory).asHierarchicalContent(with(same(dataRootFile)),
                            with(actionMatcher));

                }
            });

        hierarchicalContentProvider.asContent(dataSetCode);
        context.assertIsSatisfied();

        // check that lock is released on recorded action execution
        context.checking(new Expectations()
            {
                {
                    one(shareIdManager).releaseLock(dataSetCode);
                }
            });
        actionMatcher.recordedObject().execute();
        context.assertIsSatisfied();
    }

    @Test
    void testAsContentFromContainerCode()
    {
        final String containerCode = "CONTAINER_CODE";
        final ContainerDataSet container = new ContainerDataSet();
        container.setCode(containerCode);

        final DataSet dataSet1 = new DataSet();
        final DataSet dataSet2 = new DataSet();
        final DataSet dataSet3 = new DataSet();
        final String componentCode1 = "DS_CODE_1";
        final String componentCode2 = "DS_CODE_2";
        final String componentCode3 = "DS_CODE_3";
        dataSet1.setCode(componentCode1);
        dataSet2.setCode(componentCode2);
        dataSet3.setCode(componentCode3);
        final File dataSetRootFile1 = new File("DS_FILE_1");
        final File dataSetRootFile2 = new File("DS_FILE_2");
        final File dataSetRootFile3 = new File("DS_FILE_3");

        container.getContainedDataSets().add(dataSet1);
        container.getContainedDataSets().add(dataSet2);
        container.getContainedDataSets().add(dataSet3);

        final RecordingMatcher<IDelegatedAction> actionMatcher = RecordingMatcher.create();
        context.checking(new Expectations()
            {
                {
                    one(openbisService).tryGetDataSet(containerCode);
                    will(returnValue(container));

                    one(shareIdManager).lock(componentCode1);
                    one(directoryProvider).getDataSetDirectory(dataSet1);
                    will(returnValue(dataSetRootFile1));

                    one(shareIdManager).lock(componentCode2);
                    one(directoryProvider).getDataSetDirectory(dataSet2);
                    will(returnValue(dataSetRootFile2));

                    one(shareIdManager).lock(componentCode3);
                    one(directoryProvider).getDataSetDirectory(dataSet3);
                    will(returnValue(dataSetRootFile3));

                    IHierarchicalContent content1 = new DummyHierarchicalContent();
                    IHierarchicalContent content2 = new DummyHierarchicalContent();
                    one(hierarchicalContentFactory).asHierarchicalContent(
                            with(same(dataSetRootFile1)), with(actionMatcher));
                    will(returnValue(content1));
                    one(hierarchicalContentFactory).asHierarchicalContent(
                            with(same(dataSetRootFile2)), with(actionMatcher));
                    will(returnValue(content2));

                    // creation of content for dataSet3 fails:
                    // - its lock should be automatically released
                    // - it should not be a component of the virtual content
                    one(hierarchicalContentFactory).asHierarchicalContent(
                            with(same(dataSetRootFile3)), with(actionMatcher));
                    will(throwException(new IllegalArgumentException("")));
                    one(shareIdManager).releaseLock(componentCode3);

                    one(hierarchicalContentFactory).asVirtualHierarchicalContent(
                            Arrays.asList(content1, content2)); // no content for dataSet3
                }
            });

        hierarchicalContentProvider.asContent(containerCode);
        context.assertIsSatisfied();

        // check that locks are released on execution of recorded actions
        assertEquals(3, actionMatcher.getRecordedObjects().size());
        context.checking(new Expectations()
            {
                {
                    one(shareIdManager).releaseLock(componentCode1);
                }
            });
        actionMatcher.getRecordedObjects().get(0).execute();
        context.checking(new Expectations()
            {
                {
                    one(shareIdManager).releaseLock(componentCode2);
                }
            });
        actionMatcher.getRecordedObjects().get(1).execute();
        // context.checking(new Expectations()
        // {
        // {
        // one(shareIdManager).releaseLock(componentCode3);
        // }
        // });
        // actionMatcher.getRecordedObjects().get(2).execute();

        context.assertIsSatisfied();
    }

    @Test
    void testAsContentFromFakeDataSetCodeFails()
    {
        final String dataSetCode = "FAKE_DS_CODE";

        context.checking(new Expectations()
            {
                {
                    one(openbisService).tryGetDataSet(dataSetCode);
                    will(returnValue(null));
                }
            });

        try
        {
            hierarchicalContentProvider.asContent(dataSetCode);
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException ex)
        {
            assertEquals("Unknown data set: " + dataSetCode, ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    @SuppressWarnings("deprecation")
    @Test
    void testAsContentFromDataSetLocation()
    {
        final String code = "DS_CODE";
        final String location = "DS_LOCATION";
        final IDatasetLocation dataSetLocation = new IDatasetLocation()
            {

                public String getDataSetCode()
                {
                    return code;
                }

                public String getDataSetLocation()
                {
                    return location;
                }
            };

        final File dataRootFile = new File("DS_FILE");
        final RecordingMatcher<IDelegatedAction> actionMatcher = RecordingMatcher.create();
        context.checking(new Expectations()
            {
                {
                    one(shareIdManager).lock(code);
                    one(directoryProvider).getDataSetDirectory(dataSetLocation);
                    will(returnValue(dataRootFile));

                    one(hierarchicalContentFactory).asHierarchicalContent(with(same(dataRootFile)),
                            with(actionMatcher));
                }
            });

        hierarchicalContentProvider.asContent(dataSetLocation);
        context.assertIsSatisfied();

        // check that lock is released on recorded action execution
        context.checking(new Expectations()
            {
                {
                    one(shareIdManager).releaseLock(code);
                }
            });
        actionMatcher.recordedObject().execute();
        context.assertIsSatisfied();
    }

    @SuppressWarnings("deprecation")
    @Test
    void testAsContentFromDataSetFile()
    {
        final File dataRootFile = new File("DS_FILE");

        context.checking(new Expectations()
            {
                {
                    one(hierarchicalContentFactory).asHierarchicalContent(dataRootFile,
                            IDelegatedAction.DO_NOTHING);
                }
            });

        hierarchicalContentProvider.asContent(dataRootFile);

        context.assertIsSatisfied();

    }

    private static class DummyHierarchicalContent implements IHierarchicalContent
    {

        public IHierarchicalContentNode getRootNode()
        {
            return null;
        }

        public IHierarchicalContentNode getNode(String relativePath)
                throws IllegalArgumentException
        {
            return null;
        }

        public List<IHierarchicalContentNode> listMatchingNodes(String relativePathPattern)
        {
            return null;
        }

        public List<IHierarchicalContentNode> listMatchingNodes(String startingPath,
                String fileNamePattern)
        {
            return null;
        }

        public void close()
        {

        }

    }

}
