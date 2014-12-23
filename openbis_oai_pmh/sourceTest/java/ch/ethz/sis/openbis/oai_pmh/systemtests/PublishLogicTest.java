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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.testng.Assert;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;

/**
 * @author pkupczyk
 */
public class PublishLogicTest extends OAIPMHSystemTest
{

    @Test
    @SuppressWarnings("unchecked")
    public void testGetSpaces()
    {
        Object[] resultAndError = callLogic(adminUserSessionToken, "getSpaces", null);

        resultAndError[0] = parseJson((String) resultAndError[0]);

        ArrayList<String> result = (ArrayList<String>) resultAndError[0];
        Assert.assertEquals(result, Arrays.asList("REVIEWER-SPACE", "ADMIN-SPACE"));

        String error = (String) resultAndError[1];
        Assert.assertNull(error);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetMeshTermChildrenWithParentNull()
    {
        Object[] resultAndError = callLogic(adminUserSessionToken, "getMeshTermChildren", Collections.singletonMap("parent", null));

        resultAndError[0] = parseJson((String) resultAndError[0]);

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
        Object[] resultAndError =
                callLogic(adminUserSessionToken, "getMeshTermChildren", Collections.<String, Object> singletonMap("parent", "L01.346"));

        resultAndError[0] = parseJson((String) resultAndError[0]);

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

    @SuppressWarnings("unchecked")
    @Test
    public void testPublish()
    {
        String originalExperimentCode = "EXP-REUSE";
        String originalExperimentIdentifier = "/CISD/DEFAULT/" + originalExperimentCode;

        Publication parameters = new Publication();
        parameters.experiment = originalExperimentIdentifier;
        parameters.space = "ADMIN-SPACE";
        parameters.publicationId = "Test publication id";
        parameters.title = "Test title";
        parameters.author = "Test author";
        parameters.authorEmail = "test@email.com";
        parameters.license = "CC_BY";
        parameters.notes = "Test notes";
        parameters.meshTerms = new String[] { "B04", "B04.715" };

        Object[] resultAndError = publish(adminUserSessionToken, parameters);

        waitUntilIndexUpdaterIsIdle();

        Experiment originalExperiment = getExperimentByCode(adminUserSessionToken, originalExperimentCode);
        Experiment publicationExperiment = getExperimentByCode(adminUserSessionToken, originalExperiment.getPermId());

        Object result = resultAndError[0];
        Assert.assertEquals(result, publicationExperiment.getPermId());

        String error = (String) resultAndError[1];
        Assert.assertNull(error);

        Assert.assertEquals(publicationExperiment.getCode(), originalExperiment.getPermId());
        Assert.assertEquals(publicationExperiment.getIdentifier(), "/" + parameters.space + "/DEFAULT/" + originalExperiment.getPermId());
        Assert.assertEquals(publicationExperiment.getProperties().get("PUBLICATION_ID"), parameters.publicationId);
        Assert.assertEquals(publicationExperiment.getProperties().get("PUBLICATION_TITLE"), parameters.title);
        Assert.assertEquals(publicationExperiment.getProperties().get("PUBLICATION_AUTHOR"), parameters.author);
        Assert.assertEquals(publicationExperiment.getProperties().get("PUBLICATION_AUTHOR_EMAIL"), parameters.authorEmail);
        Assert.assertEquals(publicationExperiment.getProperties().get("PUBLICATION_LICENSE"), parameters.license);
        Assert.assertEquals(publicationExperiment.getProperties().get("PUBLICATION_NOTES"), parameters.notes);
        Assert.assertEquals(publicationExperiment.getProperties().get("PUBLICATION_MESH_TERMS"), "Viruses;B04\nPlant Viruses;B04.715\n");

        Map<String, Object> mapping = (Map<String, Object>) parseJson(publicationExperiment.getProperties().get("PUBLICATION_MAPPING"));
        Assert.assertNotNull(mapping);

        Map<String, String> experimentMapping = (Map<String, String>) mapping.get("experiment");
        Assert.assertEquals(experimentMapping.get(originalExperiment.getPermId()), publicationExperiment.getPermId());

        Map<String, String> dataSetMapping = (Map<String, String>) mapping.get("dataset");
        Map<String, DataSet> originalDataSets = getDataSetsByExperimentPermId(adminUserSessionToken, originalExperiment.getPermId());
        Map<String, DataSet> publicationDataSets = getDataSetsByExperimentPermId(adminUserSessionToken, publicationExperiment.getPermId());

        int dataSetCount = 18;
        Assert.assertEquals(dataSetMapping.size(), dataSetCount);
        Assert.assertEquals(originalDataSets.size(), dataSetCount);
        Assert.assertEquals(publicationDataSets.size(), dataSetCount);

        for (DataSet originalDataSet : originalDataSets.values())
        {
            String publicationDataSetCode = dataSetMapping.get(originalDataSet.getCode());

            Assert.assertNotNull(publicationDataSetCode, "Original data set: " + originalDataSet.getCode() + " is not in the mapping");

            DataSet publicationDataSet = publicationDataSets.get(publicationDataSetCode);

            Assert.assertNotNull(publicationDataSet, "Publication data set: " + publicationDataSet.getCode()
                    + " is in the mapping but is not connected to the publication experiment: " + publicationExperiment.getCode());

            if (originalDataSet.isContainerDataSet())
            {
                Assert.assertEquals(publicationDataSet.getDataSetTypeCode(), originalDataSet.getDataSetTypeCode());
            } else
            {
                Assert.assertEquals(publicationDataSet.getDataSetTypeCode(), "PUBLICATION_CONTAINER");
            }

            Assert.assertEquals(publicationDataSet.getContainedDataSets(), Collections.singletonList(originalDataSet));
        }
    }

}