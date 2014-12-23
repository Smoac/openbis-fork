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

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.httpclient.methods.GetMethod;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.ExperimentCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.entitytype.EntityTypePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.project.ProjectIdentifier;
import ch.systemsx.cisd.common.http.HttpTest;

/**
 * @author pkupczyk
 */
public class PublishServletTest extends OAIPMHSystemTest
{

    private Map<String, Publication> publications = new LinkedHashMap<String, Publication>();

    @Override
    @BeforeTest
    public void beforeClass()
    {
        super.beforeClass();

        ExperimentCreation creation = new ExperimentCreation();
        creation.setTypeId(new EntityTypePermId("SIRNA_HCS"));
        creation.setCode("TO_PUBLISH");
        creation.setProjectId(new ProjectIdentifier("/CISD/DEFAULT"));
        creation.setProperty("DESCRIPTION", "some description");

        getApplicationServerApi().createExperiments(adminUserSessionToken, Arrays.asList(creation));

        Publication publication = new Publication();
        publication.experiment = "/CISD/DEFAULT/TO_PUBLISH";
        publication.space = "REVIEWER-SPACE";
        publication.title = "Some publication Title";
        publication.publicationId = "Some publication Id";
        publication.author = "Some author";
        publication.authorEmail = "some@authoremail.com";
        publication.license = "None";
        publication.notes = "Some notes";
        publication.meshTerms = new String[] { "B04" };

        Object[] resultAndError = publish(adminUserSessionToken, publication);

        waitUntilIndexUpdaterIsIdle();

        publications.put((String) resultAndError[0], publication);

        Assert.assertNull(resultAndError[1]);
    }

    @Test
    public void testListItems() throws IOException
    {

        GetMethod response = callServlet(REVIEWER_USER_ID, REVIEWER_USER_PASSWORD, "verb=ListIdentifiers&metadataPrefix=oai_dc");

        Document document = HttpTest.parseResponse(response);
        NodeList nodes = HttpTest.evaluateToNodeList(document, "/OAI-PMH/ListIdentifiers/header/identifier");

        Set<String> identifiers = new HashSet<String>();
        for (int i = 0; i < nodes.getLength(); i++)
        {
            String identifier = nodes.item(i).getFirstChild().getNodeValue();
            identifiers.add(identifier);
        }

        Assert.assertEquals(identifiers, publications.keySet());
    }

    @Test
    public void testListRecords() throws IOException
    {

        GetMethod response = callServlet(REVIEWER_USER_ID, REVIEWER_USER_PASSWORD, "?verb=ListRecords&metadataPrefix=oai_dc");

        Document document = HttpTest.parseResponse(response);
        NodeList nodes = HttpTest.evaluateToNodeList(document, "/OAI-PMH/ListRecords/record/header/identifier");

        Set<String> identifiers = new HashSet<String>();
        for (int i = 0; i < nodes.getLength(); i++)
        {
            String identifier = nodes.item(i).getFirstChild().getNodeValue();
            identifiers.add(identifier);
        }

        Assert.assertEquals(identifiers, publications.keySet());
    }

    @Test
    public void testGetRecord() throws IOException
    {
        String identifier = publications.keySet().iterator().next();

        GetMethod response =
                callServlet(REVIEWER_USER_ID, REVIEWER_USER_PASSWORD, "verb=GetRecord&metadataPrefix=oai_dc&identifier=" + identifier);

        Document document = HttpTest.parseResponse(response);
        Assert.assertEquals(HttpTest.evaluateToString(document, "/OAI-PMH/GetRecord/record/header/identifier"), identifier);
    }

}
