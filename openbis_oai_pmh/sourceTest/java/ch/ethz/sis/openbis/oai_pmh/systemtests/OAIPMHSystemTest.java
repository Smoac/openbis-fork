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
import ch.systemsx.cisd.openbis.generic.shared.IServiceForDataStoreServer;
import ch.systemsx.cisd.openbis.generic.shared.ResourceNames;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationChangingService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchSubCriteria;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
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

    private static final String GENERAL_INFORMATION_CHANGING_SERVICE_URL = TestInstanceHostUtils.getOpenBISUrl()
            + IGeneralInformationChangingService.SERVICE_URL;

    private static final String DSS_SERVICE_RPC_GENERIC_URL = TestInstanceHostUtils.getDSSUrl() + "/datastore_server/rmi-dss-api-v1";

    private static final String SERVICE_FOR_DATA_STORE_SERVER_URL = TestInstanceHostUtils.getOpenBISUrl() + ResourceNames.ETL_SERVICE_URL;

    private static final String PUBLISH_SERVLET_URL = TestInstanceHostUtils.getDSSUrl() + "/publish";

    private static final long TIMEOUT = 30000;

    protected static final String ADMIN_USER_ID = "test";

    protected static final String ADMIN_USER_PASSWORD = "password";

    protected static final String REVIEWER_USER_ID = "reviewer";

    protected static final String REVIEWER_USER_PASSWORD = "password";

    private IGeneralInformationService generalInformationService;

    private IGeneralInformationChangingService generalInformationChangingService;

    private IApplicationServerApi applicationServerApi;

    private IDssServiceRpcGeneric dssServiceRpcGeneric;

    private IServiceForDataStoreServer serviceForDataStoreServer;

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
        generalInformationChangingService =
                HttpInvokerUtils.createServiceStub(IGeneralInformationChangingService.class, GENERAL_INFORMATION_CHANGING_SERVICE_URL, TIMEOUT);
        dssServiceRpcGeneric = HttpInvokerUtils.createServiceStub(IDssServiceRpcGeneric.class, DSS_SERVICE_RPC_GENERIC_URL, TIMEOUT);
        serviceForDataStoreServer = HttpInvokerUtils.createServiceStub(IServiceForDataStoreServer.class, SERVICE_FOR_DATA_STORE_SERVER_URL, TIMEOUT);
        adminUserSessionToken = generalInformationService.tryToAuthenticateForAllServices(ADMIN_USER_ID, ADMIN_USER_PASSWORD);
        reviewerUserSessionToken = generalInformationService.tryToAuthenticateForAllServices(REVIEWER_USER_ID, REVIEWER_USER_PASSWORD);
    }

    protected Experiment getExperimentByPermId(String sessionToken, String experimentPermId)
    {
        SearchCriteria criteria = new SearchCriteria();
        criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.PERM_ID, experimentPermId));

        List<Experiment> experiments = getGeneralInformationService().searchForExperiments(sessionToken, criteria);
        if (experiments == null || experiments.isEmpty())
        {
            return null;
        } else
        {
            return experiments.iterator().next();
        }
    }

    protected Experiment getExperimentByIdentifier(String sessionToken, String experimentIdentifier)
    {
        ExperimentIdentifier identifier = ExperimentIdentifierFactory.parse(experimentIdentifier);

        SearchCriteria criteria = new SearchCriteria();
        criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, identifier.getExperimentCode()));

        List<Experiment> experiments = getGeneralInformationService().searchForExperiments(sessionToken, criteria);

        for (Experiment experiment : experiments)
        {
            if (experiment.getIdentifier().equals(experimentIdentifier))
            {
                return experiment;
            }
        }

        return null;
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

    protected DataSet getDataSetsByCode(String sessionToken, String dataSetCode)
    {
        SearchCriteria criteria = new SearchCriteria();
        criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, dataSetCode));

        List<DataSet> dataSets = getGeneralInformationService().searchForDataSets(sessionToken, criteria);

        if (dataSets == null || dataSets.isEmpty())
        {
            return null;
        } else
        {
            return dataSets.iterator().next();
        }
    }

    @SuppressWarnings("unchecked")
    protected PublicationResult publish(String sessionToken, Publication publication)
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
        parameters.put("tag", publication.tag);

        String permId = callLogic(sessionToken, "publish", parameters);

        waitUntilIndexUpdaterIsIdle();

        Experiment originalExperiment = getExperimentByIdentifier(adminUserSessionToken, publication.experiment);
        Experiment publicationExperiment = getExperimentByPermId(adminUserSessionToken, permId);

        Map<String, Object> mapping = (Map<String, Object>) parseJson(publicationExperiment.getProperties().get("PUBLICATION_MAPPING"));
        Assert.assertNotNull(mapping);

        Map<String, String> experimentMapping = (Map<String, String>) mapping.get("experiment");
        Assert.assertEquals(experimentMapping.get(originalExperiment.getPermId()), publicationExperiment.getPermId());

        Map<String, String> dataSetMapping = (Map<String, String>) mapping.get("dataset");
        Map<String, DataSet> originalDataSets = getDataSetsByExperimentPermId(adminUserSessionToken, originalExperiment.getPermId());
        Map<String, DataSet> publicationDataSets = getDataSetsByExperimentPermId(adminUserSessionToken, publicationExperiment.getPermId());

        PublicationResult result = new PublicationResult();
        result.originalExperiment = originalExperiment;
        result.publicationExperiment = publicationExperiment;
        result.dataSetMapping = dataSetMapping;
        result.originalDataSetMap = originalDataSets;
        result.publicationDataSetMap = publicationDataSets;

        return result;
    }

    protected String callLogic(String sessionToken, String method, Map<String, Object> methodParameters)
    {
        try
        {
            Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.put("method", method);
            parameters.put("methodParameters", methodParameters);

            QueryTableModel result =
                    dssServiceRpcGeneric.createReportFromAggregationService(sessionToken, "publish-logic", parameters);

            List<QueryTableColumn> columns = result.getColumns();
            Assert.assertEquals(columns.size(), 1);
            Assert.assertEquals(columns.get(0).getTitle(), "RESULT");

            List<Serializable[]> rows = result.getRows();
            Assert.assertEquals(rows.size(), 1);

            String resultCellValue = (String) rows.get(0)[0];
            if (StringUtils.isEmpty(resultCellValue))
            {
                return null;
            } else
            {
                return resultCellValue;
            }
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

        public String tag;

    }

    public static class PublicationResult
    {

        private Experiment originalExperiment;

        private Experiment publicationExperiment;

        private Map<String, DataSet> originalDataSetMap;

        private Map<String, DataSet> publicationDataSetMap;

        private Map<String, String> dataSetMapping;

        public Experiment getOriginalExperiment()
        {
            return originalExperiment;
        }

        public Experiment getPublicationExperiment()
        {
            return publicationExperiment;
        }

        public DataSet getOriginalDataSet(String originalDataSetCode)
        {
            return originalDataSetMap.get(originalDataSetCode);
        }

        public DataSet getPublicationDataSetFor(String originalDataSetCode)
        {
            String publicationDataSetCode = dataSetMapping.get(originalDataSetCode);
            return publicationDataSetMap.get(publicationDataSetCode);
        }

        public Map<String, DataSet> getOriginalDataSetMap()
        {
            return originalDataSetMap;
        }

        public Map<String, DataSet> getPublicationDataSetMap()
        {
            return publicationDataSetMap;
        }

        public Map<String, String> getDataSetMapping()
        {
            return dataSetMapping;
        }

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

    protected IGeneralInformationChangingService getGeneralInformationChangingService()
    {
        return generalInformationChangingService;
    }

    protected IDssServiceRpcGeneric getDssServiceRpcGeneric()
    {
        return dssServiceRpcGeneric;
    }

    public IServiceForDataStoreServer getServiceForDataStoreServer()
    {
        return serviceForDataStoreServer;
    }

}
