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

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.io.IHierarchicalContentFactory;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.common.utilities.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
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

        final ExternalData data = new ExternalData();
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
            assertEquals("Unknown data set " + dataSetCode, ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    void testAsContentFromDataSetLocation()
    {
        final String code = "DS_CODE";
        final String location = "DS_LOCATION";
        final IDatasetLocation dataSetLocation = new IDatasetLocation()
            {

                public String getDatasetCode()
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

}
