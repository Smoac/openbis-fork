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

package ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1;

import java.util.ArrayList;
import java.util.Arrays;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.dss.screening.shared.api.internal.DssServiceRpcScreeningBatchResults;

/**
 * @author pkupczyk
 */
public class DssServiceRpcScreeningBatchResultsTest extends AssertJUnit
{

    @Test
    public void testNullResults()
    {
        DssServiceRpcScreeningBatchResults<String> results =
                new DssServiceRpcScreeningBatchResults<String>();
        results.addDataStoreResults("dss1", null);
        results.addDataStoreResults("dss2", null);

        assertTrue(results.withDuplicates().isEmpty());
        assertTrue(results.withoutDuplicates().isEmpty());
    }

    @Test
    public void testEmptyResults()
    {
        DssServiceRpcScreeningBatchResults<String> results =
                new DssServiceRpcScreeningBatchResults<String>();
        results.addDataStoreResults("dss1", new ArrayList<String>());
        results.addDataStoreResults("dss2", new ArrayList<String>());

        assertTrue(results.withDuplicates().isEmpty());
        assertTrue(results.withoutDuplicates().isEmpty());
    }

    @Test
    public void testNotEmptyResults()
    {
        DssServiceRpcScreeningBatchResults<String> results =
                new DssServiceRpcScreeningBatchResults<String>();
        results.addDataStoreResults("dss1", Arrays.asList("a", "c", "e"));
        results.addDataStoreResults("dss2", Arrays.asList("c", "d"));

        assertEquals(Arrays.asList("a", "c", "e", "c", "d"), results.withDuplicates());
        assertEquals(Arrays.asList("a", "c", "e", "d"), results.withoutDuplicates());
    }

}
