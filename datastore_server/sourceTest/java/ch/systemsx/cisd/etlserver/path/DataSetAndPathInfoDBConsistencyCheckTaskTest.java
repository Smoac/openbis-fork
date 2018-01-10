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

package ch.systemsx.cisd.etlserver.path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Level;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.PhysicalData;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetSearchCriteria;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.common.utilities.MockTimeProvider;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.MockContent;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PhysicalDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.DataSetBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.DataStoreBuilder;
import ch.systemsx.cisd.openbis.util.LogRecordingUtils;

/**
 * @author Franz-Josef Elmer
 */
public class DataSetAndPathInfoDBConsistencyCheckTaskTest extends AssertJUnit
{
    private static final String SESSION_TOKEN = "session-123";
    
    private BufferedAppender logRecorder;

    private Mockery context;

    private IApplicationServerApi service;

    private IHierarchicalContentProvider fileProvider;

    private IHierarchicalContentProvider pathInfoProvider;

    private DataSetAndPathInfoDBConsistencyCheckTask task;

    @BeforeMethod
    public void setUpMocks()
    {
        logRecorder = LogRecordingUtils.createRecorder("%-5p %c - %m%n", Level.INFO);
        context = new Mockery();
        service = context.mock(IApplicationServerApi.class);
        fileProvider = context.mock(IHierarchicalContentProvider.class, "fileProvider");
        pathInfoProvider = context.mock(IHierarchicalContentProvider.class, "pathInfoProvider");
        task =
                new DataSetAndPathInfoDBConsistencyCheckTask(fileProvider, pathInfoProvider,
                        service, new MockTimeProvider(2010, 1000)){
                            @Override
                            String login()
                            {
                                return SESSION_TOKEN;
                            }};
        context.checking(new Expectations()
            {
                {
                    allowing(service).logout(SESSION_TOKEN);
                }
            });
        Properties properties = new Properties();
        properties.setProperty(DataSetAndPathInfoDBConsistencyCheckTask.CHECKING_TIME_INTERVAL_KEY,
                "1500 msec");
        task.setUp("", properties);
    }

    @AfterMethod
    public void checkContext()
    {
        logRecorder.reset();
        // The following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testConsistentCase()
    {
        PhysicalDataSet ds1 = createDataSetBuilder().code("ds1").getDataSet();
        prepareListDataSets(new Date(2010 - 1500), ds1);
        MockContent fileContent =
                prepareContentProvider(fileProvider, "ds1", ":0:0", "a/:0:0", "a/b:34:9");
        MockContent pathInfoContent =
                prepareContentProvider(pathInfoProvider, "ds1", ":0:0", "a/:0:0", "a/b:34:9");

        task.execute();

        assertThat(logRecorder.getLogLines(), hasItem("INFO  OPERATION.DataSetAndPathInfoDBConsistencyCheckTask - "
                + "Check 1 data sets registered since 1970-01-01 01:00:00"));

        assertEquals(true, fileContent.isClosed());
        assertEquals(true, pathInfoContent.isClosed());
        context.assertIsSatisfied();
    }

    @Test
    public void testErrorCase()
    {
        PhysicalDataSet ds1 = createDataSetBuilder().code("ds1").getDataSet();
        prepareListDataSets(new Date(2010 - 1500), ds1);
        MockContent fileContent =
                prepareContentProvider(fileProvider, "ds1", ":0:0", "a/:0:0", "a/b:34:9");
        prepareContentProvider(pathInfoProvider, "ds1", new RuntimeException("Oohps!"));

        task.execute();

        assertThat(logRecorder.getLogLines(), hasItem("INFO  OPERATION.DataSetAndPathInfoDBConsistencyCheckTask - "
                + "Check 1 data sets registered since 1970-01-01 01:00:00"));
        assertThat(logRecorder.getLogLines(), hasItem("ERROR OPERATION.DataSetAndPathInfoDBConsistencyChecker - "
                + "Couldn't check consistency of the file system and "
                + "the path info database for a data set: ds1"));
        assertThat(logRecorder.getLogLines(), hasItem("java.lang.RuntimeException: Oohps!"));
        assertThat(logRecorder.getLogLines(), hasItem("ERROR NOTIFY.DataSetAndPathInfoDBConsistencyCheckTask - "
                + "File system and path info DB consistency check report "
                + "for all data sets since 1970-01-01 01:00:00"));
        assertThat(logRecorder.getLogLines(), hasItem("Error when checking datasets:"));
        assertThat(logRecorder.getLogLines(), hasItem("ERROR: \"Couldn't check consistency of the file system and "
                + "the path info database for a data set: ds1 because of the following exception: Oohps!\""));

        assertEquals(true, fileContent.isClosed());
        context.assertIsSatisfied();
    }

    @Test
    public void testInconsistentCases()
    {
        PhysicalDataSet ds1 = createDataSetBuilder().code("ds1").getDataSet();
        PhysicalDataSet ds2 = createDataSetBuilder().code("ds2").getDataSet();
        PhysicalDataSet ds3 = createDataSetBuilder().code("ds3").getDataSet();
        PhysicalDataSet ds4 = createDataSetBuilder().code("ds4").getDataSet();
        PhysicalDataSet ds5 = createDataSetBuilder().code("ds5").getDataSet();
        prepareListDataSets(new Date(2010 - 1500), ds1, ds2, ds3, ds4, ds5);
        MockContent fileContent1 =
                prepareContentProvider(fileProvider, "ds1", ":0:0", "a/:0:0", "a/b:34:9",
                        "a/c:35:2", "b:33:3", "c/:0:0");
        MockContent pathInfoContent1 =
                prepareContentProvider(pathInfoProvider, "ds1", ":0:0", "a/:0:0", "a/b:34:7",
                        "a/c:42:2", "c:2:4");
        MockContent fileContent2 = prepareContentProvider(fileProvider, "ds2");
        MockContent pathInfoContent2 = prepareContentProvider(pathInfoProvider, "ds2", ":0:0");
        MockContent fileContent3 =
                prepareContentProvider(fileProvider, "ds3", ":0:0", "a/:0:0", "a/b:34:7",
                        "a/c:42:2", "c:2:4");
        MockContent pathInfoContent3 =
                prepareContentProvider(pathInfoProvider, "ds3", ":0:0", "a/:0:0", "a/b:34:9",
                        "a/c:35:2", "b:33:3", "c/:0:0");
        MockContent fileContent4 = prepareContentProvider(fileProvider, "ds4", ":0:0");
        MockContent pathInfoContent4 = prepareContentProvider(pathInfoProvider, "ds4");
        MockContent fileContent5 = prepareContentProvider(fileProvider, "ds5");
        MockContent pathInfoContent5 = prepareContentProvider(pathInfoProvider, "ds5");

        task.execute();

        assertThat(logRecorder.getLogLines(),
                hasItem("INFO  OPERATION.DataSetAndPathInfoDBConsistencyCheckTask - Check 5 data sets registered since 1970-01-01 01:00:00"));

        assertThat(
                logRecorder.getLogLines(),
                hasItem("ERROR NOTIFY.DataSetAndPathInfoDBConsistencyCheckTask - File system and path info DB consistency check report for all data sets since 1970-01-01 01:00:00"));
        assertThat(logRecorder.getLogLines(), hasItem("Data sets checked:"));
        assertThat(logRecorder.getLogLines(), hasItem("ds1, ds2, ds3, ds4, ds5"));
        assertThat(logRecorder.getLogLines(), hasItem("Differences found:"));
        assertThat(logRecorder.getLogLines(), hasItem("Data set ds1:"));
        assertThat(logRecorder.getLogLines(),
                hasItem("- 'a/b' CRC32 checksum in the file system = 00000009 but in the path info database = 00000007"));
        assertThat(logRecorder.getLogLines(), hasItem("- 'a/c' size in the file system = 35 bytes but in the path info database = 42 bytes."));
        assertThat(logRecorder.getLogLines(), hasItem("- 'b' is on the file system but is not referenced in the path info database"));
        assertThat(logRecorder.getLogLines(), hasItem("- 'c' is a directory in the file system but a file in the path info database"));
        assertThat(logRecorder.getLogLines(), hasItem("Data set ds2:"));
        assertThat(logRecorder.getLogLines(), hasItem("- exists in the path info database but does not exist in the file system"));
        assertThat(logRecorder.getLogLines(), hasItem("Data set ds3:"));
        assertThat(logRecorder.getLogLines(),
                hasItem("- 'a/b' CRC32 checksum in the file system = 00000007 but in the path info database = 00000009"));
        assertThat(logRecorder.getLogLines(), hasItem("- 'a/c' size in the file system = 42 bytes but in the path info database = 35 bytes."));
        assertThat(logRecorder.getLogLines(), hasItem("- 'b' is referenced in the path info database but does not exist on the file system"));
        assertThat(logRecorder.getLogLines(), hasItem("- 'c' is a directory in the path info database but a file in the file system"));
        assertThat(logRecorder.getLogLines(), hasItem("Data set ds5:"));
        assertThat(logRecorder.getLogLines(), hasItem("- exists neither in the path info database nor in the file system"));

        assertEquals(true, fileContent1.isClosed());
        assertEquals(true, pathInfoContent1.isClosed());
        assertEquals(true, fileContent2.isClosed());
        assertEquals(true, pathInfoContent2.isClosed());
        assertEquals(true, fileContent3.isClosed());
        assertEquals(true, pathInfoContent3.isClosed());
        assertEquals(true, fileContent4.isClosed());
        assertEquals(true, pathInfoContent4.isClosed());
        assertEquals(true, fileContent5.isClosed());
        assertEquals(true, pathInfoContent5.isClosed());
        context.assertIsSatisfied();
    }

    private void prepareContentProvider(final IHierarchicalContentProvider provider,
            final String dataSetCode, final Exception exception)
    {
        context.checking(new Expectations()
            {
                {
                    one(provider).asContentWithoutModifyingAccessTimestamp(dataSetCode);
                    will(throwException(exception));
                }
            });
    }

    private MockContent prepareContentProvider(final IHierarchicalContentProvider provider,
            final String dataSetCode, final String... nodeDescriptions)
    {
        final MockContent content = new MockContent(nodeDescriptions);
        context.checking(new Expectations()
            {
                {
                    one(provider).asContentWithoutModifyingAccessTimestamp(dataSetCode);
                    will(returnValue(content));
                }
            });
        return content;
    }

    private void prepareListDataSets(final Date youngerThanDate, final PhysicalDataSet... dataSets)
    {
        List<DataSet> translatedDataSets = new ArrayList<DataSet>();
        for (PhysicalDataSet physicalDataSet : dataSets)
        {
            translatedDataSets.add(translate(physicalDataSet));
        }
        context.checking(new Expectations()
            {
                {
                    one(service).searchDataSets(with(SESSION_TOKEN), with(new RecordingMatcher<DataSetSearchCriteria>()), 
                            with(new RecordingMatcher<DataSetFetchOptions>()));
                    will(returnValue(new SearchResult<>(translatedDataSets, translatedDataSets.size())));
                }
            });
    }
    
    private DataSet translate(PhysicalDataSet dataSet)
    {
        DataSet translatedDataSet = new DataSet();
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withPhysicalData();
        translatedDataSet.setFetchOptions(fetchOptions);
        translatedDataSet.setCode(dataSet.getCode());
        PhysicalData physicalData = new PhysicalData();
        physicalData.setLocation(dataSet.getLocation());
        translatedDataSet.setPhysicalData(physicalData);
        return translatedDataSet;
    }

    private DataSetBuilder createDataSetBuilder()
    {
        return new DataSetBuilder().store(new DataStoreBuilder("DSS").getStore()).fileFormat("XML");
    }

}
