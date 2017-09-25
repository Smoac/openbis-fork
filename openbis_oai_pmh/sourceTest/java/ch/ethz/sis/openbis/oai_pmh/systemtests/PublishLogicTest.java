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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Transformer;
import org.testng.Assert;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.MetaprojectAssignmentsIds;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.dataset.DataSetCodeId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.metaproject.IMetaprojectId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.metaproject.MetaprojectIdentifierId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LocatorType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MetaprojectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewContainerDataSet;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.StorageFormat;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;

/**
 * @author pkupczyk
 */
public class PublishLogicTest extends OAIPMHSystemTest
{

    @Test
    @SuppressWarnings("unchecked")
    public void testGetSpaces()
    {
        String result = callLogic(adminUserSessionToken, "getSpaces", null);
        ArrayList<String> resultList = (ArrayList<String>) parseJson(result);
        Assert.assertEquals(resultList, Arrays.asList("REVIEWER-SPACE", "ADMIN-SPACE"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetTags()
    {
        Experiment originalExperiment = createExperiment();
        DataSet originalPhysical = createPhysicalDataSet(originalExperiment);
        DataSet originalPhysical2 = createPhysicalDataSet(originalExperiment);
        DataSet originalPhysical3 = createPhysicalDataSet(originalExperiment);
        createPhysicalDataSet(originalExperiment);

        MetaprojectIdentifier tag = new MetaprojectIdentifier(ADMIN_USER_ID, "TEST_TAG");
        MetaprojectIdentifier tag2 = new MetaprojectIdentifier(ADMIN_USER_ID, "TEST_TAG_2");
        MetaprojectIdentifier tag3 = new MetaprojectIdentifier(ADMIN_USER_ID, "TEST_TAG_3");

        tagDataSets(tag, originalPhysical, originalPhysical2);
        tagDataSets(tag2, originalPhysical2, originalPhysical3);
        tagDataSets(tag3, originalPhysical3);

        String result =
                callLogic(adminUserSessionToken, "getTags",
                        Collections.<String, Object> singletonMap("experiment", originalExperiment.getIdentifier()));

        ArrayList<String> resultList = (ArrayList<String>) parseJson(result);
        Assert.assertEquals(resultList, Arrays.asList("TEST_TAG", "TEST_TAG_2", "TEST_TAG_3"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetMeshTermChildrenWithParentNull()
    {
        String result = callLogic(adminUserSessionToken, "getMeshTermChildren", Collections.singletonMap("parent", null));

        ArrayList<Map<String, String>> resultList = (ArrayList<Map<String, String>>) parseJson(result);
        Collection<String> terms = CollectionUtils.collect(resultList, new Transformer<Map<String, String>, String>()
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
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetMeshTermChildrenWithParentNotNull()
    {
        String result = callLogic(adminUserSessionToken, "getMeshTermChildren", Collections.<String, Object> singletonMap("parent", "L01.346"));

        ArrayList<Map<String, String>> resultList = (ArrayList<Map<String, String>>) parseJson(result);
        Collection<String> terms = CollectionUtils.collect(resultList, new Transformer<Map<String, String>, String>()
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
    }

    @Test
    public void testPublishWithPhysicalDataSet()
    {
        // DS(E)

        Experiment originalExperiment = createExperiment();
        DataSet originalPhysical = createPhysicalDataSet(originalExperiment);

        Publication publication = publication(originalExperiment.getIdentifier(), null);
        PublicationResult result = publish(adminUserSessionToken, publication);

        Assert.assertEquals(result.getDataSetMapping().size(), 1);

        DataSet publicationDataSet = result.getPublicationDataSetFor(originalPhysical.getCode());
        Assert.assertEquals(publicationDataSet.getDataSetTypeCode(), "PUBLICATION_CONTAINER");
        AssertionUtil.assertCollectionContainsOnly(publicationDataSet.getContainedDataSets(), originalPhysical);
    }

    @Test
    public void testPublishWithPhysicalDataSetWithContainer()
    {
        // DS(E) -> CDS(E)

        Experiment originalExperiment = createExperiment();
        DataSet originalPhysical = createPhysicalDataSet(originalExperiment);
        DataSet originalContainer = createContainerDataSet(originalExperiment, originalPhysical);

        Publication publication = publication(originalExperiment.getIdentifier(), null);
        PublicationResult result = publish(adminUserSessionToken, publication);

        Assert.assertEquals(result.getDataSetMapping().size(), 1);

        DataSet physicalPublished = result.getPublicationDataSetFor(originalPhysical.getCode());
        DataSet containerPublished = result.getPublicationDataSetFor(originalContainer.getCode());

        Assert.assertNull(physicalPublished);
        Assert.assertEquals(containerPublished.getDataSetTypeCode(), originalContainer.getDataSetTypeCode());
        AssertionUtil.assertCollectionContainsOnly(containerPublished.getContainedDataSets(), originalContainer);
    }

    @Test
    public void testPublishWithPhysicalDataSetWithContainerInDifferentExperiment()
    {
        // DS(E1) -> CDS(E2), publish E1

        Experiment originalExperimentWithPhysical = createExperiment();
        Experiment originalExperimentWithContainer = createExperiment();

        DataSet originalPhysical = createPhysicalDataSet(originalExperimentWithPhysical);
        DataSet originalContainer = createContainerDataSet(originalExperimentWithContainer, originalPhysical);

        Publication publication = publication(originalExperimentWithPhysical.getIdentifier(), null);
        PublicationResult result = publish(adminUserSessionToken, publication);

        Assert.assertEquals(result.getDataSetMapping().size(), 1);

        DataSet physicalPublished = result.getPublicationDataSetFor(originalPhysical.getCode());
        DataSet containerPublished = result.getPublicationDataSetFor(originalContainer.getCode());

        Assert.assertNull(containerPublished);
        Assert.assertEquals(physicalPublished.getDataSetTypeCode(), "PUBLICATION_CONTAINER");
        AssertionUtil.assertCollectionContainsOnly(physicalPublished.getContainedDataSets(), originalPhysical);
    }

    @Test
    public void testPublishWithPhysicalDataSetWithContainerWithContainer()
    {
        // DS(E) -> CDS(E) -> CDS(E)

        Experiment originalExperiment = createExperiment();
        DataSet originalPhysical = createPhysicalDataSet(originalExperiment);
        DataSet originalContainer = createContainerDataSet(originalExperiment, originalPhysical);
        DataSet originalTopContainer = createContainerDataSet(originalExperiment, originalContainer);

        Publication publication = publication(originalExperiment.getIdentifier(), null);
        PublicationResult result = publish(adminUserSessionToken, publication);

        Assert.assertEquals(result.getDataSetMapping().size(), 1);

        DataSet physicalPublished = result.getPublicationDataSetFor(originalPhysical.getCode());
        DataSet containerPublished = result.getPublicationDataSetFor(originalContainer.getCode());
        DataSet topContainerPublished = result.getPublicationDataSetFor(originalTopContainer.getCode());

        Assert.assertNull(physicalPublished);
        Assert.assertNull(containerPublished);
        Assert.assertEquals(topContainerPublished.getDataSetTypeCode(), originalTopContainer.getDataSetTypeCode());
        AssertionUtil.assertCollectionContainsOnly(topContainerPublished.getContainedDataSets(), originalTopContainer);
    }

    @Test
    public void testPublishWithPhysicalDataSetWithContainerWithContainerInDifferentExperiment()
    {
        // DS(E1) -> CDS(E1) -> CDS(E2), publishing E1

        Experiment originalExperimentWithPhysicalAndContainer = createExperiment();
        Experiment originalExperimentWithTopContainer = createExperiment();

        DataSet originalPhysical = createPhysicalDataSet(originalExperimentWithPhysicalAndContainer);
        DataSet originalContainer = createContainerDataSet(originalExperimentWithPhysicalAndContainer, originalPhysical);
        DataSet originalTopContainer = createContainerDataSet(originalExperimentWithTopContainer, originalContainer);

        Publication publication = publication(originalExperimentWithPhysicalAndContainer.getIdentifier(), null);
        PublicationResult result = publish(adminUserSessionToken, publication);

        Assert.assertEquals(result.getDataSetMapping().size(), 1);

        DataSet physicalPublished = result.getPublicationDataSetFor(originalPhysical.getCode());
        DataSet containerPublished = result.getPublicationDataSetFor(originalContainer.getCode());
        DataSet topContainerPublished = result.getPublicationDataSetFor(originalTopContainer.getCode());

        Assert.assertNull(physicalPublished);
        Assert.assertNull(topContainerPublished);
        Assert.assertEquals(containerPublished.getDataSetTypeCode(), originalContainer.getDataSetTypeCode());
        AssertionUtil.assertCollectionContainsOnly(containerPublished.getContainedDataSets(), originalContainer);
    }

    @Test
    public void testPublishWithPhysicalDataSetWithContainerInDifferentExperimentWithContainerInSameExperiment()
    {
        // DS(E1) -> CDS(E2) -> CDS(E1), publishing E1

        Experiment originalExperimentWithPhysicalAndTopContainer = createExperiment();
        Experiment originalExperimentWithContainer = createExperiment();

        DataSet originalPhysical = createPhysicalDataSet(originalExperimentWithPhysicalAndTopContainer);
        DataSet originalContainer = createContainerDataSet(originalExperimentWithContainer, originalPhysical);
        DataSet originalTopContainer = createContainerDataSet(originalExperimentWithPhysicalAndTopContainer, originalContainer);

        Publication publication = publication(originalExperimentWithPhysicalAndTopContainer.getIdentifier(), null);
        PublicationResult result = publish(adminUserSessionToken, publication);

        Assert.assertEquals(result.getDataSetMapping().size(), 1);

        DataSet physicalPublished = result.getPublicationDataSetFor(originalPhysical.getCode());
        DataSet containerPublished = result.getPublicationDataSetFor(originalContainer.getCode());
        DataSet topContainerPublished = result.getPublicationDataSetFor(originalTopContainer.getCode());

        Assert.assertNull(physicalPublished);
        Assert.assertNull(containerPublished);
        Assert.assertEquals(topContainerPublished.getDataSetTypeCode(), originalTopContainer.getDataSetTypeCode());
        AssertionUtil.assertCollectionContainsOnly(topContainerPublished.getContainedDataSets(), originalTopContainer);
    }

    @Test
    public void testPublishWithMultipleDataSets()
    {
        Publication publication = publication("/CISD/DEFAULT/EXP-REUSE", null);
        PublicationResult result = publish(adminUserSessionToken, publication);

        Experiment originalExperiment = result.getOriginalExperiment();
        Experiment publicationExperiment = result.getPublicationExperiment();

        Assert.assertEquals(publicationExperiment.getCode(), originalExperiment.getPermId());
        Assert.assertEquals(publicationExperiment.getIdentifier(), "/" + publication.space + "/DEFAULT/" + originalExperiment.getPermId());
        Assert.assertEquals(publicationExperiment.getProperties().get("PUBLICATION_ID"), publication.publicationId);
        Assert.assertEquals(publicationExperiment.getProperties().get("PUBLICATION_TITLE"), publication.title);
        Assert.assertEquals(publicationExperiment.getProperties().get("PUBLICATION_AUTHOR"), publication.author);
        Assert.assertEquals(publicationExperiment.getProperties().get("PUBLICATION_AUTHOR_EMAIL"), publication.authorEmail);
        Assert.assertEquals(publicationExperiment.getProperties().get("PUBLICATION_LICENSE"), publication.license);
        Assert.assertEquals(publicationExperiment.getProperties().get("PUBLICATION_NOTES"), publication.notes);
        Assert.assertEquals(publicationExperiment.getProperties().get("PUBLICATION_MESH_TERMS"), "Viruses;B04\nPlant Viruses;B04.715\n");

        Assert.assertEquals(result.getOriginalDataSetMap().size(), 18);
        Assert.assertEquals(result.getPublicationDataSetMap().size(), 11);

        AssertionUtil.assertCollectionContainsOnly(result.getDataSetMapping().keySet(), "20081105092259000-18", "20081105092259000-19",
                "20081105092259000-20", "20081105092259000-21", "20081105092259000-8", "20081105092259000-9", "20081105092259900-0",
                "20081105092259900-1", "20081105092359990-2", "20110509092359990-10", "ROOT_CONTAINER");

        DataSet rootContainer = result.getOriginalDataSet("ROOT_CONTAINER");
        DataSet rootContainerPublished = result.getPublicationDataSetFor("ROOT_CONTAINER");
        Assert.assertEquals(rootContainerPublished.getDataSetTypeCode(), "CONTAINER_TYPE");
        AssertionUtil.assertCollectionContains(rootContainerPublished.getContainedDataSets(), rootContainer);

        DataSet dataSet0 = result.getOriginalDataSet("20081105092259900-0");
        DataSet dataSet0Published = result.getPublicationDataSetFor("20081105092259900-0");
        Assert.assertEquals(dataSet0Published.getDataSetTypeCode(), "PUBLICATION_CONTAINER");
        AssertionUtil.assertCollectionContains(dataSet0Published.getContainedDataSets(), dataSet0);
    }

    @Test
    public void testPublishTaggedWithPhysicalDataSetsWithAndWithoutTag()
    {
        // DS(TAG), DS()

        Experiment originalExperiment = createExperiment();
        DataSet originalPhysical = createPhysicalDataSet(originalExperiment);
        createPhysicalDataSet(originalExperiment);

        MetaprojectIdentifier tag = new MetaprojectIdentifier(ADMIN_USER_ID, "TEST_TAG");
        tagDataSets(tag, originalPhysical);

        Publication publication = publication(originalExperiment.getIdentifier(), tag.getMetaprojectName());
        PublicationResult result = publish(adminUserSessionToken, publication);

        Assert.assertEquals(result.getDataSetMapping().size(), 1);

        DataSet publicationDataSet = result.getPublicationDataSetFor(originalPhysical.getCode());
        Assert.assertEquals(publicationDataSet.getDataSetTypeCode(), "PUBLICATION_CONTAINER");
        AssertionUtil.assertCollectionContainsOnly(publicationDataSet.getContainedDataSets(), originalPhysical);
    }

    @Test
    public void testPublishTaggedWithPhysicalDataSetsWithAndWithoutTagWithContainerWithTag()
    {
        // DS(TAG) -> CDS(TAG) <- DS()

        Experiment originalExperiment = createExperiment();
        DataSet originalPhysical = createPhysicalDataSet(originalExperiment);
        DataSet originalPhysical2 = createPhysicalDataSet(originalExperiment);
        DataSet originalContainer = createContainerDataSet(originalExperiment, originalPhysical, originalPhysical2);

        MetaprojectIdentifier tag = new MetaprojectIdentifier(ADMIN_USER_ID, "TEST_TAG");
        tagDataSets(tag, originalPhysical, originalContainer);

        Publication publication = publication(originalExperiment.getIdentifier(), tag.getMetaprojectName());
        PublicationResult result = publish(adminUserSessionToken, publication);

        Assert.assertEquals(result.getDataSetMapping().size(), 1);

        DataSet containerPublished = result.getPublicationDataSetFor(originalContainer.getCode());
        Assert.assertEquals(containerPublished.getDataSetTypeCode(), originalContainer.getDataSetTypeCode());
        AssertionUtil.assertCollectionContainsOnly(containerPublished.getContainedDataSets(), originalContainer);
    }

    @Test
    public void testPublishTaggedWithPhysicalDataSetsWithAndWithoutTagWithContainerWithoutTag()
    {
        // DS(TAG) -> CDS() <- DS()

        Experiment originalExperiment = createExperiment();
        DataSet originalPhysical = createPhysicalDataSet(originalExperiment);
        DataSet originalPhysical2 = createPhysicalDataSet(originalExperiment);
        createContainerDataSet(originalExperiment, originalPhysical, originalPhysical2);

        MetaprojectIdentifier tag = new MetaprojectIdentifier(ADMIN_USER_ID, "TEST_TAG");
        tagDataSets(tag, originalPhysical);

        Publication publication = publication(originalExperiment.getIdentifier(), tag.getMetaprojectName());
        PublicationResult result = publish(adminUserSessionToken, publication);

        Assert.assertEquals(result.getDataSetMapping().size(), 1);

        DataSet publicationDataSet = result.getPublicationDataSetFor(originalPhysical.getCode());
        Assert.assertEquals(publicationDataSet.getDataSetTypeCode(), "PUBLICATION_CONTAINER");
        AssertionUtil.assertCollectionContainsOnly(publicationDataSet.getContainedDataSets(), originalPhysical);
    }

    @Test
    public void testPublishTaggedWithPhysicalDataSetWithoutTagWithContainerWithTagWithContainerWithTag()
    {
        // DS() -> CDS(TAG) -> CSD(TAG)

        Experiment originalExperiment = createExperiment();
        DataSet originalPhysical = createPhysicalDataSet(originalExperiment);
        DataSet originalContainer = createContainerDataSet(originalExperiment, originalPhysical);
        DataSet originalTopContainer = createContainerDataSet(originalExperiment, originalContainer);

        MetaprojectIdentifier tag = new MetaprojectIdentifier(ADMIN_USER_ID, "TEST_TAG");
        tagDataSets(tag, originalContainer, originalTopContainer);

        Publication publication = publication(originalExperiment.getIdentifier(), tag.getMetaprojectName());
        PublicationResult result = publish(adminUserSessionToken, publication);

        Assert.assertEquals(result.getDataSetMapping().size(), 1);

        DataSet containerTopPublished = result.getPublicationDataSetFor(originalTopContainer.getCode());
        Assert.assertEquals(containerTopPublished.getDataSetTypeCode(), originalTopContainer.getDataSetTypeCode());
        AssertionUtil.assertCollectionContainsOnly(containerTopPublished.getContainedDataSets(), originalTopContainer);
    }

    @Test
    public void testPublishTaggedWithPhysicalDataSetsWithTagsWithContainerWithTagWithContainerWithoutTag()
    {
        // DS(TAG) -> CDS(TAG) -> CSD()
        // DS(TAG) ->

        Experiment originalExperiment = createExperiment();
        DataSet originalPhysical = createPhysicalDataSet(originalExperiment);
        DataSet originalPhysical2 = createPhysicalDataSet(originalExperiment);
        DataSet originalContainer = createContainerDataSet(originalExperiment, originalPhysical, originalPhysical2);
        createContainerDataSet(originalExperiment, originalContainer);

        MetaprojectIdentifier tag = new MetaprojectIdentifier(ADMIN_USER_ID, "TEST_TAG");
        tagDataSets(tag, originalPhysical, originalPhysical2, originalContainer);

        Publication publication = publication(originalExperiment.getIdentifier(), tag.getMetaprojectName());
        PublicationResult result = publish(adminUserSessionToken, publication);

        Assert.assertEquals(result.getDataSetMapping().size(), 1);

        DataSet containerPublished = result.getPublicationDataSetFor(originalContainer.getCode());
        Assert.assertEquals(containerPublished.getDataSetTypeCode(), originalContainer.getDataSetTypeCode());
        AssertionUtil.assertCollectionContainsOnly(containerPublished.getContainedDataSets(), originalContainer);
    }

    private Publication publication(String experiment, String tag)
    {
        Publication parameters = new Publication();
        parameters.space = "ADMIN-SPACE";
        parameters.experiment = experiment;
        parameters.publicationId = "Test publication id";
        parameters.title = "Test title";
        parameters.author = "Test author";
        parameters.authorEmail = "test@email.com";
        parameters.license = "CC_BY";
        parameters.notes = "Test notes";
        parameters.meshTerms = new String[] { "B04", "B04.715" };
        parameters.tag = tag;
        return parameters;
    }

    private Experiment createExperiment()
    {
        String permId = getServiceForDataStoreServer().createPermId(adminUserSessionToken);

        ExperimentCreation creation = new ExperimentCreation();
        creation.setProjectId(new ProjectIdentifier("/CISD/DEFAULT"));
        creation.setTypeId(new EntityTypePermId("SIRNA_HCS"));
        creation.setCode(permId);
        creation.setProperty("DESCRIPTION", "some description");

        List<ExperimentPermId> permIds = getApplicationServerApi().createExperiments(adminUserSessionToken, Arrays.asList(creation));

        waitUntilIndexUpdaterIsIdle();

        return getExperimentByPermId(adminUserSessionToken, permIds.get(0).getPermId());
    }

    private DataSet createPhysicalDataSet(Experiment experiment)
    {
        String permId = getServiceForDataStoreServer().createPermId(adminUserSessionToken);

        NewExternalData newData = new NewExternalData();
        newData.setCode(permId);
        newData.setDataSetType(new DataSetType("UNKNOWN"));
        newData.setDataSetKind(DataSetKind.PHYSICAL);
        newData.setFileFormatType(new FileFormatType("PROPRIETARY"));
        newData.setLocation("some/location/" + permId);
        newData.setLocatorType(new LocatorType("RELATIVE_LOCATION"));
        newData.setStorageFormat(StorageFormat.PROPRIETARY);
        newData.setDataStoreCode("STANDARD");

        getServiceForDataStoreServer().registerDataSet(adminUserSessionToken, ExperimentIdentifierFactory.parse(experiment.getIdentifier()), newData);
        waitUntilIndexUpdaterIsIdle();

        return getDataSetsByCode(adminUserSessionToken, permId);
    }

    private DataSet createContainerDataSet(Experiment experiment, DataSet... contained)
    {
        String permId = getServiceForDataStoreServer().createPermId(adminUserSessionToken);

        NewContainerDataSet newData = new NewContainerDataSet();
        newData.setCode(permId);
        newData.setDataSetType(new DataSetType("CONTAINER_TYPE"));
        newData.setDataSetKind(DataSetKind.CONTAINER);
        newData.setDataStoreCode("STANDARD");

        List<String> containedCodes = new LinkedList<String>();
        for (DataSet aContained : contained)
        {
            containedCodes.add(aContained.getCode());
        }
        newData.setContainedDataSetCodes(containedCodes);

        getServiceForDataStoreServer().registerDataSet(adminUserSessionToken, ExperimentIdentifierFactory.parse(experiment.getIdentifier()), newData);
        waitUntilIndexUpdaterIsIdle();

        return getDataSetsByCode(adminUserSessionToken, permId);
    }

    private void tagDataSets(MetaprojectIdentifier tagIdentifier, DataSet... dataSets)
    {
        IMetaprojectId tagId = new MetaprojectIdentifierId(tagIdentifier);

        String sessionToken = null;
        if (ADMIN_USER_ID.equals(tagIdentifier.getMetaprojectOwnerId()))
        {
            sessionToken = adminUserSessionToken;
        } else if (REVIEWER_USER_ID.equals(tagIdentifier.getMetaprojectOwnerId()))
        {
            sessionToken = reviewerUserSessionToken;
        } else
        {
            throw new IllegalArgumentException("Unsupported tag owner: " + tagIdentifier.getMetaprojectOwnerId());
        }

        List<Metaproject> tags = getGeneralInformationService().listMetaprojects(sessionToken);
        boolean tagExists = false;

        for (Metaproject tag : tags)
        {
            if (tag.getOwnerId().equals(tagIdentifier.getMetaprojectOwnerId()) && tag.getName().equals(tagIdentifier.getMetaprojectName()))
            {
                tagExists = true;
                break;
            }
        }

        if (false == tagExists)
        {
            getGeneralInformationChangingService().createMetaproject(sessionToken, tagIdentifier.getMetaprojectName(), null);
        }

        MetaprojectAssignmentsIds assignmentIds = new MetaprojectAssignmentsIds();
        for (DataSet dataSet : dataSets)
        {
            assignmentIds.addDataSet(new DataSetCodeId(dataSet.getCode()));
        }
        getGeneralInformationChangingService().addToMetaproject(sessionToken, tagId, assignmentIds);
    }

}