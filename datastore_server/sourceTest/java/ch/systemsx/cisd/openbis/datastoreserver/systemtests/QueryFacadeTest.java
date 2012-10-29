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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SpaceWithProjectsAndRoleAssignments;
import ch.systemsx.cisd.openbis.plugin.query.client.api.v1.FacadeFactory;
import ch.systemsx.cisd.openbis.plugin.query.client.api.v1.IQueryApiFacade;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.AggregationServiceDescription;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryTableColumn;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryTableModel;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.ReportDescription;

/**
 * @author Chandrasekhar Ramakrishnan
 */
@Test(groups = "slow")
public class QueryFacadeTest extends SystemTestCase
{
    private static final String OPENBIS_URL = "http://localhost:8888";

    private IQueryApiFacade queryFacade;

    private IQueryApiFacade observerFacade;

    @BeforeMethod
    public void beforeMethod()
    {
        queryFacade = createServiceFacade("test");
        observerFacade = createServiceFacade("observer");
    }

    @Test
    public void testAggregationServiceReport() throws Exception
    {
        AggregationServiceDescription service =
                getAggregationServiceDescription("example-aggregation-service");
        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("name", "world");

        QueryTableModel table = queryFacade.createReportFromAggregationService(service, parameters);

        assertEquals("[String, Integer]", getHeaders(table).toString());
        assertEquals("[Hello, 20]", Arrays.asList(table.getRows().get(0)).toString());
        assertEquals("[world, 30]", Arrays.asList(table.getRows().get(1)).toString());
        assertEquals(2, table.getRows().size());
    }

    @Test
    public void testJythonAggregationServiceReport() throws Exception
    {
        AggregationServiceDescription service =
                getAggregationServiceDescription("example-jython-aggregation-service-report");
        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("name", "world");

        QueryTableModel table = queryFacade.createReportFromAggregationService(service, parameters);

        assertEquals("[Key, Value]", getHeaders(table).toString());
        assertEquals("[name, world]", Arrays.asList(table.getRows().get(0)).toString());
        assertEquals(1, table.getRows().size());
        assertEquals(
                "[From: datastore_server@localhost, To: franz-josef.elmer@systemsx.ch, Subject: test, Content:]",
                FileUtilities.loadToStringList(getLatestEmail()).subList(1, 5).toString());
    }

    @Test
    public void testJythonReportWhichSendsEMail() throws Exception
    {
        new File(store, "1/xml/result-8").mkdirs();
        ReportDescription description = getTableReportDescription("example-jython-report");

        QueryTableModel table =
                queryFacade.createReportFromDataSets(description,
                        Arrays.asList("20081105092259000-8"));

        assertEquals("[Data Set, Data Set Type]", getHeaders(table).toString());
        assertEquals("[20081105092259000-8, HCS_IMAGE]", Arrays.asList(table.getRows().get(0))
                .toString());
        assertEquals(1, table.getRows().size());
        assertEquals(
                "[From: datastore_server@localhost, To: franz-josef.elmer@systemsx.ch, Subject: test, Content:]",
                FileUtilities.loadToStringList(getLatestEmail()).subList(1, 5).toString());
    }

    @Test
    public void testDbModifyingAggregationServiceReport() throws Exception
    {
        AggregationServiceDescription service =
                getAggregationServiceDescription("example-db-modifying-aggregation-service");
        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("name", "world");

        QueryTableModel table = queryFacade.createReportFromAggregationService(service, parameters);

        assertEquals("[String, Integer]", getHeaders(table).toString());
        assertEquals("[Hello, 20]", Arrays.asList(table.getRows().get(0)).toString());
        assertEquals("[world, 30]", Arrays.asList(table.getRows().get(1)).toString());
        assertEquals(2, table.getRows().size());

        IGeneralInformationService generalInformationService =
                queryFacade.getGeneralInformationService();
        List<SpaceWithProjectsAndRoleAssignments> spaces =
                generalInformationService.listSpacesWithProjectsAndRoleAssignments(
                        queryFacade.getSessionToken(), null);
        boolean foundSpace = false;
        for (SpaceWithProjectsAndRoleAssignments space : spaces)
        {
            if ("NEWDUMMYSPACE".equalsIgnoreCase(space.getCode()))
            {
                foundSpace = true;
            }
        }

        assertTrue("Did not find a space called [NEWDUMMYSPACE]", foundSpace);
    }

    @Test
    public void testJythonDbModifyingAggregationServiceReport() throws Exception
    {
        AggregationServiceDescription service =
                getAggregationServiceDescription("example-jython-db-modifying-aggregation-service");
        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("code", "JYTHON-TEST");

        QueryTableModel table = queryFacade.createReportFromAggregationService(service, parameters);

        assertEquals("[CODE, IDENTIFIER]", getHeaders(table).toString());
        assertEquals("[JYTHON-TEST, /CISD/JYTHON-TEST]", Arrays.asList(table.getRows().get(0))
                .toString());
        assertEquals(1, table.getRows().size());

        IGeneralInformationService generalInformationService =
                queryFacade.getGeneralInformationService();

        List<Sample> samples =
                generalInformationService.listSamplesForExperiment(queryFacade.getSessionToken(),
                        "/CISD/NEMO/EXP-TEST-1");
        boolean foundSample = false;
        for (Sample sample : samples)
        {
            if ("JYTHON-TEST".equalsIgnoreCase(sample.getCode()))
            {
                foundSample = true;
            }
        }

        assertTrue("Did not find a sample called [JYTHON-TEST]", foundSample);
    }

    /**
     * The observer trying to access the forbidden dataset via the authorized content provider.
     */
    @Test(expectedExceptions = Exception.class)
    public void testJythonAggregationServiceWithContentProviderAuthentication() throws Exception
    {
        AggregationServiceDescription service =
                getAggregationServiceDescription("content-provider-aggregation-service");
        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("dataset-code", "20081105092159111-1");

        File content = new File(new File(new File(store, "42"), "a"), "1");
        content.mkdirs();

        observerFacade.createReportFromAggregationService(service, parameters);
    }

    /**
     * The testcase, where the observer tries to acces the dataset that he cannot see, but through
     * the non-authorized content provider.
     */
    @Test
    public void testJythonAggregationServiceWithoutContentProviderAuthentication() throws Exception
    {
        AggregationServiceDescription service =
                getAggregationServiceDescription("content-provider-aggregation-service-no-authorization");
        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("dataset-code", "20081105092159111-1");

        File content = new File(new File(new File(store, "42"), "a"), "1");
        content.mkdirs();

        QueryTableModel table =
                observerFacade.createReportFromAggregationService(service, parameters);

        assertEquals("[name]", getHeaders(table).toString());
        assertEquals("[1]", Arrays.asList(table.getRows().get(0)).toString());
        assertEquals(1, table.getRows().size());
    }

    /**
     * The authorized user tries to access the dataset via the authorized content provider.
     */
    @Test
    public void testJythonAggregationServiceWithContentProviderAuthenticationAndAuthorizedUser()
            throws Exception
    {
        AggregationServiceDescription service =
                getAggregationServiceDescription("content-provider-aggregation-service");
        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("dataset-code", "20081105092159111-1");

        File content = new File(new File(new File(store, "42"), "a"), "1");
        content.mkdirs();

        QueryTableModel table =
 queryFacade.createReportFromAggregationService(service, parameters);

        assertEquals("[name]", getHeaders(table).toString());
        assertEquals("[1]", Arrays.asList(table.getRows().get(0)).toString());
        assertEquals(1, table.getRows().size());
    }

    private IQueryApiFacade createServiceFacade(String userName)
    {
        return FacadeFactory.create(OPENBIS_URL, userName, "a");
    }

    private ReportDescription getTableReportDescription(String key)
    {
        List<ReportDescription> services = queryFacade.listTableReportDescriptions();
        for (ReportDescription description : services)
        {
            if (description.getKey().equals(key))
            {
                return description;
            }
        }
        throw new AssertionError("No reporting for key '" + key + "'.");
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

    private List<String> getHeaders(QueryTableModel tableModel)
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

    private File getLatestEmail()
    {
        File latestFile = null;
        File[] files = new File("targets/email").listFiles();
        for (File file : files)
        {
            if (latestFile == null || latestFile.lastModified() < file.lastModified())
            {
                latestFile = file;
            }
        }
        return latestFile;
    }
}
