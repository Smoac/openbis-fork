/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.plugins;

import static ch.systemsx.cisd.common.test.ArrayContainsExactlyMatcher.containsExactly;
import static ch.systemsx.cisd.etlserver.plugins.FillUnknownDataSetSizeInOpenbisDBFromPathInfoDBMaintenanceTask.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.collections.map.HashedMap;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.utilities.ITimeProvider;
import ch.systemsx.cisd.etlserver.path.IPathsInfoDAO;
import ch.systemsx.cisd.etlserver.path.PathEntryDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;

/**
 * @author pkupczyk
 */
public class FillUnknownDataSetSizeInOpenbisDBFromPathInfoDBMaintenanceTaskTest
{

    private Mockery context;

    private IEncapsulatedOpenBISService service;

    private IPathsInfoDAO dao;

    private ITimeProvider timeProvider;

    private SimpleDataSetInformationDTO dataSet1;

    private SimpleDataSetInformationDTO dataSet2;

    private SimpleDataSetInformationDTO dataSet3;

    private PathEntryDTO entry1;

    private PathEntryDTO entry2;

    @BeforeMethod
    public void beforeMethod()
    {
        context = new Mockery();
        service = context.mock(IEncapsulatedOpenBISService.class);
        dao = context.mock(IPathsInfoDAO.class);
        timeProvider = context.mock(ITimeProvider.class);

        dataSet1 = new SimpleDataSetInformationDTO();
        dataSet1.setDataSetCode("DS_1");
        dataSet2 = new SimpleDataSetInformationDTO();
        dataSet2.setDataSetCode("DS_2");
        dataSet3 = new SimpleDataSetInformationDTO();
        dataSet3.setDataSetCode("DS_3");

        entry1 = new PathEntryDTO();
        entry1.setDataSetCode("DS_1");
        entry1.setSizeInBytes(123L);

        entry2 = new PathEntryDTO();
        entry2.setDataSetCode("DS_2");
    }

    @AfterMethod
    public void afterMethod()
    {
        context.assertIsSatisfied();
    }

    @Test
    public void testExecuteWhereListOfDataSetsWithUnknownSizeInOpenbisDBIsEmpty()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(timeProvider).getTimeInMilliseconds();
                    will(returnValue(0L));

                    one(service).listPhysicalDataSetsWithUnknownSize(DEFAULT_CHUNK_SIZE);
                    will(returnValue(Collections.emptyList()));
                }
            });

        execute(null, null);
    }

    @Test
    public void testExecuteWhereListOfDataSetsWithUnknownSizeInOpenbisDBIsNull()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(timeProvider).getTimeInMilliseconds();
                    will(returnValue(0L));

                    one(service).listPhysicalDataSetsWithUnknownSize(DEFAULT_CHUNK_SIZE);
                    will(returnValue(null));
                }
            });

        execute(null, null);
    }

    @Test
    public void testExecuteWhereListOfDataSetSizesFoundInPathinfoDBIsEmpty()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(timeProvider).getTimeInMilliseconds();
                    will(returnValue(0L));

                    one(service).listPhysicalDataSetsWithUnknownSize(DEFAULT_CHUNK_SIZE);
                    will(returnValue(Arrays.asList(dataSet1)));

                    one(dao).listDataSetsSize(new String[] { dataSet1.getDataSetCode() });
                    will(returnValue(Collections.emptyList()));

                    one(service).listPhysicalDataSetsWithUnknownSize(DEFAULT_CHUNK_SIZE);
                    will(returnValue(Collections.emptyList()));
                }
            });

        execute(null, null);
    }

    @Test
    public void testExecuteWhereListOfDataSetSizesFoundInPathinfoDBIsNull()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(timeProvider).getTimeInMilliseconds();
                    will(returnValue(0L));

                    one(service).listPhysicalDataSetsWithUnknownSize(DEFAULT_CHUNK_SIZE);
                    will(returnValue(Arrays.asList(dataSet1)));

                    one(dao).listDataSetsSize(new String[] { dataSet1.getDataSetCode() });
                    will(returnValue(null));

                    one(service).listPhysicalDataSetsWithUnknownSize(DEFAULT_CHUNK_SIZE);
                    will(returnValue(Collections.emptyList()));
                }
            });

        execute(null, null);
    }

    @Test
    public void testExecuteWithOneChunk()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(timeProvider).getTimeInMilliseconds();
                    will(returnValue(0L));

                    one(service).listPhysicalDataSetsWithUnknownSize(DEFAULT_CHUNK_SIZE);
                    will(returnValue(Arrays.asList(dataSet1, dataSet2, dataSet3)));

                    one(dao).listDataSetsSize(
                            with(containsExactly(dataSet1.getDataSetCode(), dataSet2.getDataSetCode(), dataSet3
                                    .getDataSetCode())));
                    will(returnValue(Arrays.asList(entry1, entry2)));

                    Map<String, Long> sizeMap = new HashedMap<String, Long>();
                    sizeMap.put("DS_1", 123L);
                    one(service).updatePhysicalDataSetsSize(sizeMap);

                    one(service).listPhysicalDataSetsWithUnknownSize(DEFAULT_CHUNK_SIZE);
                    will(returnValue(Collections.emptyList()));
                }
            });

        execute(null, null);
    }

    @Test
    public void testExecuteWithMultipleChunks()
    {
        final int chunkSize = 2;

        context.checking(new Expectations()
            {
                {
                    allowing(timeProvider).getTimeInMilliseconds();
                    will(returnValue(0L));

                    one(service).listPhysicalDataSetsWithUnknownSize(chunkSize);
                    will(returnValue(Arrays.asList(dataSet1, dataSet2)));

                    one(dao).listDataSetsSize(
                            with(containsExactly(dataSet1.getDataSetCode(), dataSet2.getDataSetCode())));
                    will(returnValue(Arrays.asList(entry1, entry2)));

                    Map<String, Long> sizeMap = new HashedMap<String, Long>();
                    sizeMap.put("DS_1", 123L);
                    one(service).updatePhysicalDataSetsSize(sizeMap);

                    one(service).listPhysicalDataSetsWithUnknownSize(chunkSize);
                    will(returnValue(Arrays.asList(dataSet3)));

                    one(dao).listDataSetsSize(new String[] { dataSet3.getDataSetCode() });
                    will(returnValue(Collections.emptyList()));

                    one(service).listPhysicalDataSetsWithUnknownSize(chunkSize);
                    will(returnValue(Collections.emptyList()));
                }
            });

        execute(null, chunkSize);
    }

    @Test
    public void testExecuteWithTimeLimit()
    {
        final long timeLimit = 10L;
        final int chunkSize = 1;

        context.checking(new Expectations()
            {
                {
                    one(timeProvider).getTimeInMilliseconds();
                    will(returnValue(0L));

                    one(service).listPhysicalDataSetsWithUnknownSize(chunkSize);
                    will(returnValue(Arrays.asList(dataSet1)));

                    one(dao).listDataSetsSize(new String[] { dataSet1.getDataSetCode() });
                    will(returnValue(Arrays.asList(entry1)));

                    Map<String, Long> sizeMap = new HashedMap<String, Long>();
                    sizeMap.put("DS_1", 123L);
                    one(service).updatePhysicalDataSetsSize(sizeMap);

                    one(timeProvider).getTimeInMilliseconds();
                    will(returnValue(8L));

                    one(service).listPhysicalDataSetsWithUnknownSize(chunkSize);
                    will(returnValue(Arrays.asList(dataSet2)));

                    one(dao).listDataSetsSize(new String[] { dataSet2.getDataSetCode() });
                    will(returnValue(Collections.emptyList()));

                    one(timeProvider).getTimeInMilliseconds();
                    will(returnValue(12L));
                }
            });

        execute(timeLimit, chunkSize);
    }

    private void execute(Long timeLimit, Integer chunkSize)
    {
        FillUnknownDataSetSizeInOpenbisDBFromPathInfoDBMaintenanceTask task =
                new FillUnknownDataSetSizeInOpenbisDBFromPathInfoDBMaintenanceTask(service, dao, timeProvider);

        Properties properties = new Properties();

        if (timeLimit != null)
        {
            properties.setProperty(TIME_LIMIT_KEY, timeLimit.toString() + " ms");
        }
        if (chunkSize != null)
        {
            properties.setProperty(CHUNK_SIZE_KEY, chunkSize.toString());
        }

        task.setUp("fill-unknown-sizes", properties);
        task.execute();
    }
}
