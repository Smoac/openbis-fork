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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang.StringUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.IDssServiceRpcGeneric;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;
import ch.systemsx.cisd.openbis.generic.shared.util.TestInstanceHostUtils;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryTableColumn;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryTableModel;

/**
 * @author pkupczyk
 */
public class PublishLogicTest extends OAIPMHSystemTest
{

    private static final String GENERAL_INFORMATION_SERVICE_URL = TestInstanceHostUtils.getOpenBISUrl() + IGeneralInformationService.SERVICE_URL;

    private static final String DSS_SERVICE_RPC_GENERIC_URL = TestInstanceHostUtils.getDSSUrl() + "/datastore_server/rmi-dss-api-v1";

    private static final long TIMEOUT = 30000;

    private static final String USER_ID = "test";

    private static final String USER_PASSWORD = "password";

    private IGeneralInformationService generalInformationService;

    private IDssServiceRpcGeneric dssServiceRpcGeneric;

    private String sessionToken;

    @BeforeClass
    public void beforeClass()
    {
        generalInformationService = HttpInvokerUtils.createServiceStub(IGeneralInformationService.class, GENERAL_INFORMATION_SERVICE_URL, TIMEOUT);
        dssServiceRpcGeneric = HttpInvokerUtils.createServiceStub(IDssServiceRpcGeneric.class, DSS_SERVICE_RPC_GENERIC_URL, TIMEOUT);
        sessionToken = generalInformationService.tryToAuthenticateForAllServices(USER_ID, USER_PASSWORD);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetSpaces()
    {
        Object[] resultAndError = call("getSpaces", null);

        ArrayList<String> result = (ArrayList<String>) resultAndError[0];
        Assert.assertEquals(result, Arrays.asList("PUBLICATIONS_1", "PUBLICATIONS_2"));

        String error = (String) resultAndError[1];
        Assert.assertNull(error);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetMeshTermChildrenWithParentNull()
    {
        Object[] resultAndError = call("getMeshTermChildren", Collections.singletonMap("parent", null));

        ArrayList<Map<String, String>> result = (ArrayList<Map<String, String>>) resultAndError[0];
        Collection<String> terms = CollectionUtils.collect(result, new Transformer<Map<String, String>, String>()
            {
                @Override
                public String transform(Map<String, String> input)
                {
                    Assert.assertEquals(input.get("fullName"), "/" + input.get("name"));
                    Assert.assertEquals(input.get("hasChildren"), true);
                    return input.get("name") + ";" + input.get("identifier");
                }
            });
        Assert.assertEquals(terms, Arrays.asList("Anatomy;A", "Organisms;B", "Diseases;C", "Chemicals and Drugs;D",
                "Analytical,Diagnostic and Therapeutic Techniques and Equipment;E", "Psychiatry and Psychology;F", "Phenomena and Processes;G",
                "Disciplines and Occupations;H", "Anthropology,Education,Sociology and Social Phenomena;I", "Technology,Industry,Agriculture;J",
                "Humanities;K", "Information Science;L", "Named Groups;M", "Health Care;N", "Publication Characteristics;V", "Geographicals;Z"));

        String error = (String) resultAndError[1];
        Assert.assertNull(error);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetMeshTermChildrenWithParentNotNull()
    {
        Object[] resultAndError = call("getMeshTermChildren", Collections.<String, Object> singletonMap("parent", "L01.346"));

        ArrayList<Map<String, String>> result = (ArrayList<Map<String, String>>) resultAndError[0];
        Collection<String> terms = CollectionUtils.collect(result, new Transformer<Map<String, String>, String>()
            {
                @Override
                public String transform(Map<String, String> input)
                {
                    return input.get("name") + ";" + input.get("fullName") + ";" + input.get("identifier") + ";"
                            + String.valueOf(input.get("hasChildren"));
                }
            });
        Assert.assertEquals(terms, Arrays.asList("Archives;/Information Science/Information Science/Information Centers/Archives;L01.346.208;false",
                "Libraries;/Information Science/Information Science/Information Centers/Libraries;L01.346.596;true"));

        String error = (String) resultAndError[1];
        Assert.assertNull(error);
    }

    @Test
    public void testPublish()
    {
        String originalExperimentIdentifier = "/CISD/DEFAULT/EXP-REUSE";
        String originalExperimentPermId = "200811050940555-1032";

        String publicationSpace = "PUBLICATIONS_1";
        String publicationId = "Test publication id";
        String publicationTitle = "Test title";
        String publicationAuthor = "Test author";
        String publicationAuthorEmail = "test@email.com";
        String publicationLicence = "CC_BY";
        String publicationNotes = "Test notes";
        String[] publicationMeshTerms = new String[] { "B04", "B04.715" };

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("experiment", originalExperimentIdentifier);
        parameters.put("space", publicationSpace);
        parameters.put("publicationId", publicationId);
        parameters.put("title", publicationTitle);
        parameters.put("author", publicationAuthor);
        parameters.put("authorEmail", publicationAuthorEmail);
        parameters.put("license", publicationLicence);
        parameters.put("notes", publicationNotes);
        parameters.put("meshTerms", publicationMeshTerms);

        Object[] resultAndError = call("publish", parameters);

        waitUntilIndexUpdaterIsIdle();

        Object result = resultAndError[1];
        Assert.assertNull(result);

        String error = (String) resultAndError[1];
        Assert.assertNull(error);

        // the publication experiment should have a code equal to a perm id of the original experiment
        SearchCriteria criteria = new SearchCriteria();
        criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, originalExperimentPermId));

        List<Experiment> publications = getGeneralInformationService().searchForExperiments(sessionToken, criteria);
        Assert.assertEquals(publications.size(), 1);

        Experiment publication = publications.get(0);
        Assert.assertEquals(publication.getCode(), originalExperimentPermId);
        Assert.assertEquals(publication.getIdentifier(), "/" + publicationSpace + "/DEFAULT/" + originalExperimentPermId);
        Assert.assertEquals(publication.getProperties().get("PUBLICATION_ID"), publicationId);
        Assert.assertEquals(publication.getProperties().get("PUBLICATION_TITLE"), publicationTitle);
        Assert.assertEquals(publication.getProperties().get("PUBLICATION_AUTHOR"), publicationAuthor);
        Assert.assertEquals(publication.getProperties().get("PUBLICATION_AUTHOR_EMAIL"), publicationAuthorEmail);
        Assert.assertEquals(publication.getProperties().get("PUBLICATION_LICENSE"), publicationLicence);
        Assert.assertEquals(publication.getProperties().get("PUBLICATION_NOTES"), publicationNotes);
        Assert.assertEquals(publication.getProperties().get("PUBLICATION_MESH_TERMS"), "Viruses;B04\nPlant Viruses;B04.715\n");
    }

    private Object[] call(String method, Map<String, Object> methodParameters)
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

            ObjectMapper json = new ObjectMapper();
            Object resultCellValue = rows.get(0)[0];
            Object errorCellValue = rows.get(0)[1];

            Object[] resultAndError = new Object[2];
            resultAndError[0] = StringUtils.isEmpty((String) resultCellValue) ? null : json.readValue((String) resultCellValue, Object.class);
            resultAndError[1] = StringUtils.isEmpty((String) errorCellValue) ? null : errorCellValue;
            return resultAndError;
        } catch (Exception e)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(e);
        }
    }

}