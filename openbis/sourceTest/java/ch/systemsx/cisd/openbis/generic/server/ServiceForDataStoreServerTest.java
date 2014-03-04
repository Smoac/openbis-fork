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

package ch.systemsx.cisd.openbis.generic.server;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;
import ch.systemsx.cisd.openbis.systemtest.SystemTestCase;

/**
 * @author pkupczyk
 */
public class ServiceForDataStoreServerTest extends SystemTestCase
{

    @Test()
    public void testListPhysicalDataSetsWithUnknownSize()
    {
        String sessionToken = authenticateAs("test");
        List<SimpleDataSetInformationDTO> dataSetsWithUnknownSize = etlService.listPhysicalDataSetsWithUnknownSize(sessionToken, "STANDARD");

        sortByCode(dataSetsWithUnknownSize);
        Assert.assertEquals(20, dataSetsWithUnknownSize.size());
        Assert.assertEquals("20081105092159188-3", dataSetsWithUnknownSize.get(0).getDataSetCode());
        Assert.assertEquals("VALIDATIONS_PARENT-28", dataSetsWithUnknownSize.get(dataSetsWithUnknownSize.size() - 1).getDataSetCode());
    }

    @Test(dependsOnMethods = "testListPhysicalDataSetsWithUnknownSize")
    public void testUpdatePhysicalDataSetsWithUnknownSize()
    {
        String sessionToken = authenticateAs("test");

        Map<String, Long> sizeMap = new HashMap<String, Long>();
        sizeMap.put("20081105092159188-3", 123L);

        etlService.updatePhysicalDataSetsSize(sessionToken, sizeMap);

        List<SimpleDataSetInformationDTO> dataSetsWithUnknownSize = etlService.listPhysicalDataSetsWithUnknownSize(sessionToken, "STANDARD");
        List<AbstractExternalData> updatedDataSets = etlService.listDataSetsByCode(sessionToken, Arrays.asList("20081105092159188-3"));

        sortByCode(dataSetsWithUnknownSize);
        Assert.assertEquals(19, dataSetsWithUnknownSize.size());
        Assert.assertEquals("20081105092159222-2", dataSetsWithUnknownSize.get(0).getDataSetCode());
        Assert.assertEquals("VALIDATIONS_PARENT-28", dataSetsWithUnknownSize.get(dataSetsWithUnknownSize.size() - 1).getDataSetCode());

        Assert.assertEquals(1, updatedDataSets.size());
        Assert.assertEquals("20081105092159188-3", updatedDataSets.get(0).getCode());
        Assert.assertEquals(Long.valueOf(123L), updatedDataSets.get(0).getSize());
    }

    private void sortByCode(List<SimpleDataSetInformationDTO> dataSets)
    {
        Collections.sort(dataSets, new Comparator<SimpleDataSetInformationDTO>()
            {
                @Override
                public int compare(SimpleDataSetInformationDTO o1, SimpleDataSetInformationDTO o2)
                {
                    return o1.getDataSetCode().compareTo(o2.getDataSetCode());
                }
            });
    }

}
