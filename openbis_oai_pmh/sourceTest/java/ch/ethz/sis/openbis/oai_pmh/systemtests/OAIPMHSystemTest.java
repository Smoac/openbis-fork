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

package ch.ethz.sis.openbis.oai_pmh.systemtests;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.StringUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.ethz.sis.openbis.generic.shared.api.v3.IApplicationServerApi;
import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.http.HttpTest;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.GenericSystemTest;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.IDssServiceRpcGeneric;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchSubCriteria;
import ch.systemsx.cisd.openbis.generic.shared.util.TestInstanceHostUtils;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryTableColumn;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryTableModel;

/**
 * @author pkupczyk
 */
public abstract class OAIPMHSystemTest extends GenericSystemTest
{

    private static final String APPLICATION_SERVER_API_URL = TestInstanceHostUtils.getOpenBISUrl() + IApplicationServerApi.SERVICE_URL;

    private static final String GENERAL_INFORMATION_SERVICE_URL = TestInstanceHostUtils.getOpenBISUrl() + IGeneralInformationService.SERVICE_URL;

    private static final String DSS_SERVICE_RPC_GENERIC_URL = TestInstanceHostUtils.getDSSUrl() + "/datastore_server/rmi-dss-api-v1";

    private static final String PUBLISH_SERVLET_URL = TestInstanceHostUtils.getDSSUrl() + "/publish";

    private static final long TIMEOUT = 30000;

    protected static final String ADMIN_USER_ID = "test";

    protected static final String ADMIN_USER_PASSWORD = "password";

    protected static final String REVIEWER_USER_ID = "reviewer";

    protected static final String REVIEWER_USER_PASSWORD = "password";

    private IGeneralInformationService generalInformationService;

    private IApplicationServerApi applicationServerApi;

    private IDssServiceRpcGeneric dssServiceRpcGeneric;

    protected String adminUserSessionToken;

    protected String reviewerUserSessionToken;

    @Override
    @BeforeSuite
    public void beforeSuite() throws Exception
    {
        OAIPMHTestInitializer.init();
        super.beforeSuite();
    }

    @BeforeClass
    public void beforeClass()
    {
        applicationServerApi = HttpInvokerUtils.createServiceStub(IApplicationServerApi.class, APPLICATION_SERVER_API_URL, TIMEOUT);
        generalInformationService = HttpInvokerUtils.createServiceStub(IGeneralInformationService.class, GENERAL_INFORMATION_SERVICE_URL, TIMEOUT);
        dssServiceRpcGeneric = HttpInvokerUtils.createServiceStub(IDssServiceRpcGeneric.class, DSS_SERVICE_RPC_GENERIC_URL, TIMEOUT);
        adminUserSessionToken = generalInformationService.tryToAuthenticateForAllServices(ADMIN_USER_ID, ADMIN_USER_PASSWORD);
        reviewerUserSessionToken = generalInformationService.tryToAuthenticateForAllServices(REVIEWER_USER_ID, REVIEWER_USER_PASSWORD);
    }

    protected Experiment getExperimentByCode(String sessionToken, String experimentCode)
    {
        SearchCriteria criteria = new SearchCriteria();
        criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, experimentCode));

        List<Experiment> experiments = getGeneralInformationService().searchForExperiments(sessionToken, criteria);
        if (experiments == null || experiments.isEmpty())
        {
            return null;
        } else if (experiments.size() == 1)
        {
            return experiments.get(0);
        } else
        {
            throw new IllegalArgumentException("More than one experiment found for code: " + experimentCode);
        }
    }

    protected Map<String, DataSet> getDataSetsByExperimentPermId(String sessionToken, String experimentPermId)
    {
        SearchCriteria experimentCriteria = new SearchCriteria();
        experimentCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.PERM_ID, experimentPermId));

        SearchCriteria dataSetCriteria = new SearchCriteria();
        dataSetCriteria.addSubCriteria(SearchSubCriteria.createExperimentCriteria(experimentCriteria));

        List<DataSet> dataSets = getGeneralInformationService().searchForDataSets(sessionToken, dataSetCriteria);

        Map<String, DataSet> map = new HashMap<String, DataSet>();
        for (DataSet dataSet : dataSets)
        {
            map.put(dataSet.getCode(), dataSet);
        }
        return map;
    }

    protected Object[] publish(String sessionToken, Publication publication)
    {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("experiment", publication.experiment);
        parameters.put("space", publication.space);
        parameters.put("publicationId", publication.publicationId);
        parameters.put("title", publication.title);
        parameters.put("author", publication.author);
        parameters.put("authorEmail", publication.authorEmail);
        parameters.put("license", publication.license);
        parameters.put("notes", publication.notes);
        parameters.put("meshTerms", publication.meshTerms);

        return callLogic(sessionToken, "publish", parameters);
    }

    protected Object[] callLogic(String sessionToken, String method, Map<String, Object> methodParameters)
    {
        try
        {
            Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.put("method", method);
            parameters.put("methodParameters", methodParameters);

            QueryTableModel result =
                    dssServiceRpcGeneric.createReportFromAggregationService(sessionToken, "publish-logic", parameters);

            List<QueryTableColumn> columns = result.getColumns();
            Assert.assertEquals(columns.size(), 2);
            Assert.assertEquals(columns.get(0).getTitle(), "RESULT");
            Assert.assertEquals(columns.get(1).getTitle(), "ERROR");

            List<Serializable[]> rows = result.getRows();
            Assert.assertEquals(rows.size(), 1);

            Object resultCellValue = rows.get(0)[0];
            Object errorCellValue = rows.get(0)[1];

            Object[] resultAndError = new Object[2];
            resultAndError[0] = StringUtils.isEmpty((String) resultCellValue) ? null : resultCellValue;
            resultAndError[1] = StringUtils.isEmpty((String) errorCellValue) ? null : errorCellValue;
            return resultAndError;
        } catch (Exception e)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(e);
        }
    }

    protected GetMethod callServlet(String userId, String userPassword, String query)
    {
        return HttpTest.sendRequest(userId, userPassword, PUBLISH_SERVLET_URL + "?" + query);
    }

    protected Object parseJson(String jsonString)
    {
        try
        {
            return StringUtils.isEmpty(jsonString) ? null : new ObjectMapper().readValue(jsonString, Object.class);
        } catch (Exception e)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(e);
        }
    }

    public static class Publication
    {

        public String experiment;

        public String space;

        public String publicationId;

        public String title;

        public String author;

        public String authorEmail;

        public String license;

        public String notes;

        public String[] meshTerms;

    }

    protected IApplicationServerApi getApplicationServerApi()
    {
        return applicationServerApi;
    }

    @Override
    protected IGeneralInformationService getGeneralInformationService()
    {
        return generalInformationService;
    }

    protected IDssServiceRpcGeneric getDssServiceRpcGeneric()
    {
        return dssServiceRpcGeneric;
    }

}
