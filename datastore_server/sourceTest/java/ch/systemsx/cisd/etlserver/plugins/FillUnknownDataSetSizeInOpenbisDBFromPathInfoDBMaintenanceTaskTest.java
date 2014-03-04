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

import java.util.Arrays;
import java.util.Map;

import org.apache.commons.collections.map.HashedMap;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.Test;

import ch.systemsx.cisd.etlserver.path.IPathsInfoDAO;
import ch.systemsx.cisd.etlserver.path.PathEntryDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;

/**
 * @author pkupczyk
 */
public class FillUnknownDataSetSizeInOpenbisDBFromPathInfoDBMaintenanceTaskTest
{

    @Test
    public void test()
    {
        Mockery context = new Mockery();
        final IEncapsulatedOpenBISService service = context.mock(IEncapsulatedOpenBISService.class);
        final IPathsInfoDAO dao = context.mock(IPathsInfoDAO.class);

        final SimpleDataSetInformationDTO dataSet1 = new SimpleDataSetInformationDTO();
        dataSet1.setDataSetCode("DS_1");
        final SimpleDataSetInformationDTO dataSet2 = new SimpleDataSetInformationDTO();
        dataSet2.setDataSetCode("DS_2");
        final SimpleDataSetInformationDTO dataSet3 = new SimpleDataSetInformationDTO();
        dataSet3.setDataSetCode("DS_3");

        final PathEntryDTO entry1 = new PathEntryDTO();
        entry1.setDataSetCode("DS_1");
        entry1.setSizeInBytes(123L);

        final PathEntryDTO entry2 = new PathEntryDTO();
        entry2.setDataSetCode("DS_2");

        context.checking(new Expectations()
            {
                {
                    one(service).listPhysicalDataSetsWithUnknownSize();
                    will(returnValue(Arrays.asList(dataSet1, dataSet2, dataSet3)));

                    one(dao).listDataSetsSize(
                            with(containsExactly(dataSet1.getDataSetCode(), dataSet2.getDataSetCode(), dataSet3
                                    .getDataSetCode())));
                    will(returnValue(Arrays.asList(entry1, entry2)));

                    Map<String, Long> sizeMap = new HashedMap<String, Long>();
                    sizeMap.put("DS_1", 123L);
                    one(service).updatePhysicalDataSetsSize(sizeMap);
                }
            });

        FillUnknownDataSetSizeInOpenbisDBFromPathInfoDBMaintenanceTask task =
                new FillUnknownDataSetSizeInOpenbisDBFromPathInfoDBMaintenanceTask(service, dao);
        task.execute();

        context.assertIsSatisfied();
    }
}
