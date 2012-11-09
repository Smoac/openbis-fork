/*
 * Copyright 2012 ETH Zuerich, CISD
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeMethod;

import ch.systemsx.cisd.openbis.plugin.query.client.api.v1.FacadeFactory;
import ch.systemsx.cisd.openbis.plugin.query.client.api.v1.IQueryApiFacade;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.AggregationServiceDescription;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryTableColumn;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryTableModel;

/**
 * 
 *
 * @author Jakub Straszewski
 */
public class AbstractQueryFacadeTest extends SystemTestCase
{
    private static final String OPENBIS_URL = "http://localhost:8888";

    protected IQueryApiFacade queryFacade;

    @BeforeMethod
    public void beforeMethod()
    {
        queryFacade = createServiceFacade("test");
    }

    private IQueryApiFacade createServiceFacade(String userName)
    {
        return FacadeFactory.create(OPENBIS_URL, userName, "a");
    }

    private AggregationServiceDescription getAggregationServiceDescription(String key)
    {
        List<AggregationServiceDescription> services = queryFacade.listAggregationServices();
        for (AggregationServiceDescription aggregationServiceDescription : services)
        {
            if (aggregationServiceDescription.getServiceKey().equals(key))
            {
                return aggregationServiceDescription;
            }
        }
        throw new AssertionError("No aggregation service for key '" + key + "'.");
    }

    protected QueryTableModel createReportFromAggregationService(String serviceCode,
            Map<String, Object> parameters)
    {
        return createReportFromAggregationService("test", serviceCode, parameters);
    }

    protected QueryTableModel createReportFromAggregationService(String user, String serviceCode,
            Map<String, Object> parameters)
    {
        AggregationServiceDescription service = getAggregationServiceDescription(serviceCode);
        return createServiceFacade(user).createReportFromAggregationService(service, parameters);
    }

    protected List<String> getHeaders(QueryTableModel tableModel)
    {
        List<QueryTableColumn> columns = tableModel.getColumns();
        List<String> headers = new ArrayList<String>();
        for (QueryTableColumn column : columns)
        {
            String header = column.getTitle();
            headers.add(header);
        }
        return headers;
    }
}
