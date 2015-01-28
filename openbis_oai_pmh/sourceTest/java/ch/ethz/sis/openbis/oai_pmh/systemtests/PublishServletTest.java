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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.FileUtils;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.testng.mustache.Mustache;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.ExperimentCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.entitytype.EntityTypePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.project.ProjectIdentifier;
import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.http.HttpTest;
import ch.systemsx.cisd.common.utilities.TestResources;
import ch.systemsx.cisd.openbis.generic.shared.util.TestInstanceHostUtils;

/**
 * @author pkupczyk
 */
public class PublishServletTest extends OAIPMHSystemTest
{

    private Map<String, Publication> publications = new LinkedHashMap<String, Publication>();

    private TestResources resources = new TestResources(getClass());

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
    public void testListIdentifiers() throws IOException, SAXException
    {
        GetMethod response = callServlet(REVIEWER_USER_ID, REVIEWER_USER_PASSWORD, "verb=ListIdentifiers&metadataPrefix=oai_dc");

        Document document = HttpTest.parseResponse(response);
        String responseDate = HttpTest.evaluateToString(document, "/OAI-PMH/responseDate");
        List<String> datestamps = HttpTest.evaluateToStrings(document, "/OAI-PMH/ListIdentifiers/header/datestamp");

        Map<String, Object> templateValues = new HashMap<String, Object>();
        templateValues.put("RESPONSE_DATE", responseDate);

        List<Record> records = new ArrayList<Record>();
        int i = 0;
        for (String permId : publications.keySet())
        {
            records.add(new Record(permId, datestamps.get(i)));
            i++;
        }
        templateValues.put("RECORDS", records);

        String expectedResponse = getFilledTemplate("testListIdentifiers.xml", templateValues);
        assertResponse(response.getResponseBodyAsString(), expectedResponse);
    }

    @Test
    public void testListRecords() throws IOException, SAXException
    {

        GetMethod response = callServlet(REVIEWER_USER_ID, REVIEWER_USER_PASSWORD, "?verb=ListRecords&metadataPrefix=oai_dc");

        Document document = HttpTest.parseResponse(response);
        String responseDate = HttpTest.evaluateToString(document, "/OAI-PMH/responseDate");
        List<String> datestamps = HttpTest.evaluateToStrings(document, "/OAI-PMH/ListRecords/record/header/datestamp");

        Map<String, Object> templateValues = new HashMap<String, Object>();
        templateValues.put("RESPONSE_DATE", responseDate);

        List<Record> records = new ArrayList<Record>();
        int i = 0;
        for (String permId : publications.keySet())
        {
            records.add(new Record(permId, datestamps.get(i)));
            i++;
        }
        templateValues.put("RECORDS", records);

        String expectedResponse = getFilledTemplate("testListRecords.xml", templateValues);
        assertResponse(response.getResponseBodyAsString(), expectedResponse);
    }

    @Test
    public void testGetRecord() throws IOException, SAXException
    {
        String identifier = publications.keySet().iterator().next();

        GetMethod response =
                callServlet(REVIEWER_USER_ID, REVIEWER_USER_PASSWORD, "verb=GetRecord&metadataPrefix=oai_dc&identifier=" + identifier);

        Document document = HttpTest.parseResponse(response);
        String responseDate = HttpTest.evaluateToString(document, "/OAI-PMH/responseDate");
        String datestamp = HttpTest.evaluateToString(document, "/OAI-PMH/GetRecord/record/header/datestamp");

        Map<String, Object> templateValues = new HashMap<String, Object>();
        templateValues.put("RESPONSE_DATE", responseDate);
        templateValues.put("IDENTIFIER", identifier);
        templateValues.put("DATESTAMP", datestamp);

        String expectedResponse = getFilledTemplate("testGetRecord.xml", templateValues);
        assertResponse(response.getResponseBodyAsString(), expectedResponse);
    }

    private String getFilledTemplate(String templateFileName, Map<String, Object> values)
    {
        values.put("SERVER_URL", TestInstanceHostUtils.getOpenBISUrl());

        try
        {
            File templateFile = resources.getResourceFile(templateFileName);
            return new Mustache().run(FileUtils.readFileToString(templateFile), values);
        } catch (Exception e)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(e);
        }
    }

    private void assertResponse(String actualResponse, String expectedResponse) throws SAXException, IOException
    {
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreAttributeOrder(true);

        System.out.println("Actual response:\n" + actualResponse);
        System.out.println("Expected response:\n" + expectedResponse);

        Diff diff = XMLUnit.compareXML(actualResponse, expectedResponse);
        Assert.assertTrue(diff.identical(), diff.toString());
    }

    public static class Record
    {

        public String IDENTIFIER;

        public String DATESTAMP;

        public Record(String IDENTIFIER, String DATESTAMP)
        {
            this.IDENTIFIER = IDENTIFIER;
            this.DATESTAMP = DATESTAMP;
        }

    }

}
