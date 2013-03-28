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

import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.logging.LogUtils;
import ch.systemsx.cisd.common.utilities.MockTimeProvider;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.MockContent;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PhysicalDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.DataSetBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.DataStoreBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;
import ch.systemsx.cisd.openbis.generic.shared.translator.DataSetTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.SimpleDataSetHelper;

/**
 * @author Franz-Josef Elmer
 */
public class DataSetAndPathInfoDBConsistencyCheckTaskTest extends AssertJUnit
{
    private BufferedAppender logRecorder;

    private Mockery context;

    private IEncapsulatedOpenBISService service;

    private IHierarchicalContentProvider fileProvider;

    private IHierarchicalContentProvider pathInfoProvider;

    private DataSetAndPathInfoDBConsistencyCheckTask task;

    @BeforeMethod
    public void setUpMocks()
    {
        logRecorder = new BufferedAppender("%-5p %c - %m%n", Level.INFO);
        context = new Mockery();
        service = context.mock(IEncapsulatedOpenBISService.class);
        fileProvider = context.mock(IHierarchicalContentProvider.class, "fileProvider");
        pathInfoProvider = context.mock(IHierarchicalContentProvider.class, "pathInfoProvider");
        task =
                new DataSetAndPathInfoDBConsistencyCheckTask(fileProvider, pathInfoProvider,
                        service, new MockTimeProvider(2010, 1000));
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

        assertEquals("INFO  OPERATION.DataSetAndPathInfoDBConsistencyCheckTask - "
                + "Check 1 data sets registered since 1970-01-01 01:00:00", logRecorder.getLogContent());
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

        assertEquals("INFO  OPERATION.DataSetAndPathInfoDBConsistencyCheckTask - "
                + "Check 1 data sets registered since 1970-01-01 01:00:00\n"
                + "ERROR OPERATION.DataSetAndPathInfoDBConsistencyChecker - "
                + "Couldn't check consistency of the file system and "
                + "the path info database for a data set: ds1\n"
                + "java.lang.RuntimeException: Oohps!\n"
                + "ERROR NOTIFY.DataSetAndPathInfoDBConsistencyCheckTask - "
                + "File system and path info DB consistency check report "
                + "for all data sets since 1970-01-01 01:00:00\n\n"
                + "Error when checking datasets:\n\n"
                + "ERROR: \"Couldn't check consistency of the file system and "
                + "the path info database for a data set: ds1 "
                + "because of the following exception: Oohps!\"",
                LogUtils.removeEmbeddedStackTrace(logRecorder.getLogContent()));
        assertEquals(true, fileContent.isClosed());
        context.assertIsSatisfied();
    }

    @Test
    public void testInconstitentCases()
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

        assertEquals(
                "INFO  OPERATION.DataSetAndPathInfoDBConsistencyCheckTask - "
                        + "Check 5 data sets registered since 1970-01-01 01:00:00\n"
                        + "ERROR NOTIFY.DataSetAndPathInfoDBConsistencyCheckTask - "
                        + "File system and path info DB consistency check report "
                        + "for all data sets since 1970-01-01 01:00:00\n\n"
                        + "Data sets checked:\n\nds1, ds2, ds3, ds4, ds5\n\n"
                        + "Differences found:\n\n"
                        + "Data set ds1:\n"
                        + "- 'a/b' CRC32 checksum in the file system = 00000009 but in the path info database = 00000007\n"
                        + "- 'a/c' size in the file system = 35 bytes but in the path info database = 42 bytes.\n"
                        + "- 'b' is on the file system but is not referenced in the path info database\n"
                        + "- 'c' is a directory in the file system but a file in the path info database\n\n"
                        + "Data set ds2:\n"
                        + "- exists in the path info database but does not exist in the file system\n\n"
                        + "Data set ds3:\n"
                        + "- 'a/b' CRC32 checksum in the file system = 00000007 but in the path info database = 00000009\n"
                        + "- 'a/c' size in the file system = 42 bytes but in the path info database = 35 bytes.\n"
                        + "- 'b' is referenced in the path info database but does not exist on the file system\n"
                        + "- 'c' is a directory in the path info database but a file in the file system\n\n"
                        + "Data set ds5:\n"
                        + "- exists neither in the path info database nor in the file system",
                logRecorder.getLogContent());
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
                    one(provider).asContent(dataSetCode);
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
                    one(provider).asContent(dataSetCode);
                    will(returnValue(content));
                }
            });
        return content;
    }

    private void prepareListDataSets(final Date youngerThanDate, final PhysicalDataSet... dataSets)
    {
        final List<SimpleDataSetInformationDTO> translatedDataSets =
                new ArrayList<SimpleDataSetInformationDTO>();
        for (PhysicalDataSet physicalDataSet : dataSets)
        {
            translatedDataSets.add(SimpleDataSetHelper.translate(DataSetTranslator
                    .translateToDescription(physicalDataSet)));
        }
        context.checking(new Expectations()
            {
                {
                    one(service).listOldestPhysicalDataSets(youngerThanDate, Integer.MAX_VALUE);
                    will(returnValue(translatedDataSets));
                }
            });
    }

    private DataSetBuilder createDataSetBuilder()
    {
        return new DataSetBuilder().store(new DataStoreBuilder("DSS").getStore()).fileFormat("XML");
    }

}
