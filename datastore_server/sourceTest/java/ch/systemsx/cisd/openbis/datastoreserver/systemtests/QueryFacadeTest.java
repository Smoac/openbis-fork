/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.datastoreserver.systemtests;

import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.plugin.query.client.api.v1.FacadeFactory;
import ch.systemsx.cisd.openbis.plugin.query.client.api.v1.IQueryApiFacade;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.AggregationServiceDescription;

/**
 * @author Chandrasekhar Ramakrishnan
 */
@Test(groups = "slow")
public class QueryFacadeTest extends SystemTestCase
{
    private static final String OPENBIS_URL = "http://localhost:8888";

    private IQueryApiFacade queryFacade;

    @BeforeMethod
    public void beforeMethod()
    {
        queryFacade = createServiceFacade("test");
    }

    @Test
    public void testAggregationServiceReport() throws Exception
    {
        List<AggregationServiceDescription> services = queryFacade.listAggregationServices();
        assertEquals(0, services.size());
    }

    private IQueryApiFacade createServiceFacade(String userName)
    {
        return FacadeFactory.create(OPENBIS_URL, userName, "a");
    }
}
