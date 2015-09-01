/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

package ch.ethz.sis.openbis.systemtest.api.v3;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.ArchivingStatus;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.Complete;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.DataSet;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.ExternalData;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.FileFormatType;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.LocatorType;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.vocabulary.Vocabulary;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.vocabulary.VocabularyTerm;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.dataset.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.dataset.ExternalDataFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.vocabulary.VocabularyFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.vocabulary.VocabularyTermFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.dataset.DataSetPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.entitytype.EntityTypePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.tag.TagPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.DataSetSearchCriterion;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.SampleSearchCriterion;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.SearchResult;

/**
 * @author pkupczyk
 */
public class SearchDataSetTest extends AbstractDataSetTest
{
    @Test
    public void testSearchWithIdSetToPermId()
    {
        DataSetSearchCriterion criterion = new DataSetSearchCriterion();
        criterion.withId().thatEquals(new DataSetPermId("20081105092259000-18"));
        testSearch(TEST_USER, criterion, "20081105092259000-18");
    }

    @Test
    public void testSearchWithPermId()
    {
        DataSetSearchCriterion criterion = new DataSetSearchCriterion();
        criterion.withPermId().thatEquals("20081105092259000-18");
        testSearch(TEST_USER, criterion, "20081105092259000-18");
    }

    @Test
    public void testSearchWithCode()
    {
        DataSetSearchCriterion criterion = new DataSetSearchCriterion();
        criterion.withCode().thatEquals("20081105092259000-18");
        testSearch(TEST_USER, criterion, "20081105092259000-18");
    }

    @Test
    public void testSearchTwoDataSetsWithCodeAndId()
    {
        DataSetSearchCriterion criterion = new DataSetSearchCriterion();
        criterion.withOrOperator();
        criterion.withCode().thatEquals("20081105092259000-18");
        criterion.withCode().thatEquals("20081105092259000-19");
        testSearch(TEST_USER, criterion, "20081105092259000-18", "20081105092259000-19");
    }

    @Test
    public void testSearchWithProperty()
    {
        DataSetSearchCriterion criterion = new DataSetSearchCriterion();
        criterion.withProperty("COMMENT").thatContains("non-virt");
        testSearch(TEST_USER, criterion, "20110509092359990-11", "20110509092359990-12");
    }

    @Test
    public void testSearchWithRegistrationDateIsEarlierThan()
    {
        DataSetSearchCriterion criterion = new DataSetSearchCriterion();
        criterion.withRegistrationDate().thatIsEarlierThanOrEqualTo("2008-11-05 09:22:00");
        testSearch(TEST_USER, criterion, "20081105092159188-3");
    }

    @Test
    public void testSearchWithModicationDateIsLaterThan()
    {
        DataSetSearchCriterion criterion = new DataSetSearchCriterion();
        criterion.withModificationDate().thatIsLaterThanOrEqualTo("2011-05-01");
        criterion.withContainer().withCode().thatContains("2");
        testSearch(TEST_USER, criterion, "20110509092359990-11", "COMPONENT_2A", "20110509092359990-12");
    }

    @Test
    public void testSearchWithContainer()
    {
        DataSetSearchCriterion criterion = new DataSetSearchCriterion();
        criterion.withContainer().withPermId().thatEquals("20110509092359990-10");
        testSearch(TEST_USER, criterion, "20110509092359990-11", "20110509092359990-12");
    }

    @Test
    public void testSearchWithChildren()
    {
        DataSetSearchCriterion criterion = new DataSetSearchCriterion();
        criterion.withChildren().withCode().thatEquals("20081105092259000-9");
        testSearch(TEST_USER, criterion, "20081105092159111-1", "20081105092159222-2", "20081105092159333-3");
    }

    @Test
    public void testSearchWithChildrenWithPropertyEquals()
    {
        DataSetSearchCriterion criterion = new DataSetSearchCriterion();
        criterion.withProperty("GENDER").thatEquals("FEMALE");
        criterion.withChildren().withCode().thatEquals("20081105092259000-9");
        testSearch(TEST_USER, criterion, "20081105092159111-1");
    }

    @Test
    public void testSearchWithParent()
    {
        DataSetSearchCriterion criterion = new DataSetSearchCriterion();
        criterion.withParents().withCode().thatEquals("20081105092159111-1");
        testSearch(TEST_USER, criterion, "20081105092259000-9");
    }

    @Test
    public void testSearchWithExperiment()
    {
        DataSetSearchCriterion criterion = new DataSetSearchCriterion();
        criterion.withCode().thatStartsWith("20120628092259000");
        criterion.withExperiment();
        testSearch(TEST_USER, criterion, "20120628092259000-24", "20120628092259000-25");
    }

    @Test
    public void testSearchWithoutExperiment()
    {
        DataSetSearchCriterion criterion = new DataSetSearchCriterion();
        criterion.withoutExperiment();
        testSearch(TEST_USER, criterion, "20120628092259000-23");
    }

    @Test
    public void testSearchWithExperimentWithPermIdThatEquals()
    {
        DataSetSearchCriterion criterion = new DataSetSearchCriterion();
        criterion.withExperiment().withPermId().thatEquals("200902091255058-1035");
        testSearch(TEST_USER, criterion, "20081105092159333-3", "20110805092359990-17");
    }

    @Test
    public void testSearchWithExperimentWithProperty()
    {
        DataSetSearchCriterion criterion = new DataSetSearchCriterion();
        criterion.withExperiment().withProperty("GENDER");
        testSearch(TEST_USER, criterion, "20081105092159333-3", "20110805092359990-17", "20081105092159188-3");
    }

    @Test
    public void testSearchWithExperimentWithPropertyThatEquals()
    {
        DataSetSearchCriterion criterion = new DataSetSearchCriterion();
        criterion.withExperiment().withProperty("GENDER").thatEquals("MALE");
        testSearch(TEST_USER, criterion, "20081105092159188-3");
    }

    @Test
    public void testSearchWithExperimentYoungerThan()
    {
        DataSetSearchCriterion criterion = new DataSetSearchCriterion();
        criterion.withExperiment().withRegistrationDate().thatIsLaterThanOrEqualTo("2009-02-09 12:11:00");
        testSearch(TEST_USER, criterion, "20081105092159333-3", "20110805092359990-17");
    }

    @Test
    public void testSearchWithSample()
    {
        DataSetSearchCriterion criterion = new DataSetSearchCriterion();
        criterion.withCode().thatStartsWith("20120628092259000");
        criterion.withSample();
        testSearch(TEST_USER, criterion, "20120628092259000-23");
    }

    @Test
    public void testSearchWithoutSample()
    {
        DataSetSearchCriterion criterion = new DataSetSearchCriterion();
        criterion.withCode().thatStartsWith("20120628092259000");
        criterion.withoutSample();
        testSearch(TEST_USER, criterion, "20120628092259000-24", "20120628092259000-25");
    }

    @Test
    public void testSearchWithSampleWithPropertiesThatContains()
    {
        DataSetSearchCriterion criterion = new DataSetSearchCriterion();
        SampleSearchCriterion sampleSearchCriterion = criterion.withSample().withOrOperator();
        sampleSearchCriterion.withProperty("BACTERIUM").thatContains("M-X");
        sampleSearchCriterion.withProperty("ORGANISM").thatContains("LY");
        testSearch(TEST_USER, criterion, "20081105092159111-1", "20081105092159333-3", "20110805092359990-17");
    }

    @Test
    public void testSearchWithFetchOptionExperiment()
    {
        DataSetSearchCriterion criterion = new DataSetSearchCriterion();
        criterion.withPermId().thatEquals("20110805092359990-17");
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withExperiment().withProperties();

        List<DataSet> dataSets = searchDataSets(TEST_USER, criterion, fetchOptions);

        Collections.sort(dataSets, DATA_SET_COMPARATOR);
        assertEquals(dataSets.get(0).getCode(), "20110805092359990-17");
        assertEquals(dataSets.get(0).getPermId().toString(), "20110805092359990-17");
        assertEqualsDate(dataSets.get(0).getAccessDate(), "2014-04-01 09:56:25");
        assertEqualsDate(dataSets.get(0).getModificationDate(), "2009-03-23 15:34:44");
        assertEqualsDate(dataSets.get(0).getRegistrationDate(), "2009-02-09 12:21:47");
        assertEquals(dataSets.get(0).isDerived(), Boolean.FALSE);
        assertEquals(dataSets.get(0).isPlaceholder(), Boolean.FALSE);
        assertParentsNotFetched(dataSets.get(0));
        assertChildrenNotFetched(dataSets.get(0));
        assertContainersNotFetched(dataSets.get(0));
        assertContainedNotFetched(dataSets.get(0));
        assertEquals(dataSets.get(0).getExperiment().getIdentifier().toString(), "/CISD/NEMO/EXP-TEST-2");
        assertEquals(new TreeMap<String, String>(dataSets.get(0).getExperiment().getProperties()).toString(),
                "{DESCRIPTION=very important expertiment, GENDER=FEMALE, PURCHASE_DATE=2009-02-09 10:00:00 +0100}");
        assertEquals(dataSets.size(), 1);
    }

    @Test
    public void testSearchWithFetchOptionExternalDataForPhysicalDataSet()
    {
        DataSetSearchCriterion criterion = new DataSetSearchCriterion();
        criterion.withPermId().thatEquals("20081105092159111-1");
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();

        ExternalDataFetchOptions externalDataFetchOptions = fetchOptions.withExternalData();
        externalDataFetchOptions.withFileFormatType();
        externalDataFetchOptions.withLocatorType();

        VocabularyTermFetchOptions storageFormatTermFetchOptions = externalDataFetchOptions.withStorageFormat();
        storageFormatTermFetchOptions.withRegistrator();

        VocabularyFetchOptions storageFormatVocabularyFetchOptions = storageFormatTermFetchOptions.withVocabulary();
        storageFormatVocabularyFetchOptions.withRegistrator();

        List<DataSet> dataSets = searchDataSets(TEST_USER, criterion, fetchOptions);

        assertEquals(dataSets.size(), 1);
        DataSet dataSet = dataSets.get(0);

        assertEquals(dataSet.getCode(), "20081105092159111-1");

        ExternalData externalData = dataSet.getExternalData();
        assertEquals(externalData.getShareId(), "42");
        assertEquals(externalData.getLocation(), "a/1");
        assertEquals(externalData.getSize(), Long.valueOf(4711));
        assertEquals(externalData.getComplete(), Complete.UNKNOWN);
        assertEquals(externalData.getStatus(), ArchivingStatus.AVAILABLE);
        assertFalse(externalData.isPresentInArchive());
        assertFalse(externalData.isStorageConfirmation());

        FileFormatType fileFormatType = externalData.getFileFormatType();
        assertEquals(fileFormatType.getCode(), "TIFF");
        assertEquals(fileFormatType.getDescription(), "TIFF File");

        LocatorType locatorType = externalData.getLocatorType();
        assertEquals(locatorType.getCode(), "RELATIVE_LOCATION");
        assertEquals(locatorType.getDescription(), "Relative Location");

        VocabularyTerm storageFormatTerm = externalData.getStorageFormat();
        assertEquals(storageFormatTerm.getCode(), "PROPRIETARY");
        assertEquals(storageFormatTerm.getLabel(), null);
        assertEquals(storageFormatTerm.getDescription(), null);
        assertEquals(storageFormatTerm.getOrdinal(), Long.valueOf(1));
        assertTrue(storageFormatTerm.isOfficial());
        assertEquals(storageFormatTerm.getRegistrator().getUserId(), "system");
        assertEqualsDate(storageFormatTerm.getRegistrationDate(), "2008-11-05 09:18:00");
        assertEqualsDate(storageFormatTerm.getModificationDate(), "2008-11-05 09:18:00");

        Vocabulary storageFormatVocabulary = storageFormatTerm.getVocabulary();
        assertEquals(storageFormatVocabulary.getCode(), "$STORAGE_FORMAT");
        assertEquals(storageFormatVocabulary.getDescription(), "The on-disk storage format of a data set");
        assertEquals(storageFormatVocabulary.getRegistrator().getUserId(), "system");
        assertEqualsDate(storageFormatVocabulary.getRegistrationDate(), "2008-11-05 09:18:00");
        assertEqualsDate(storageFormatVocabulary.getModificationDate(), "2009-03-23 15:34:44");
    }

    @Test
    public void testSearchWithFetchOptionExternalDataForContainerDataSet()
    {
        DataSetSearchCriterion criterion = new DataSetSearchCriterion();
        criterion.withPermId().thatEquals("ROOT_CONTAINER");

        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withExternalData();

        List<DataSet> dataSets = searchDataSets(TEST_USER, criterion, fetchOptions);

        assertEquals(dataSets.size(), 1);
        DataSet dataSet = dataSets.get(0);

        assertEquals(dataSet.getCode(), "ROOT_CONTAINER");
        assertEquals(dataSet.getExternalData(), null);
    }

    @Test
    public void testSearchWithTypeWithIdSetToPermId()
    {
        DataSetSearchCriterion criterion = new DataSetSearchCriterion();
        criterion.withType().withId().thatEquals(new EntityTypePermId("LINK_TYPE"));
        testSearch(TEST_USER, criterion, "20120628092259000-23", "20120628092259000-24", "20120628092259000-25");
    }

    @Test
    public void testSearchWithTypeWithCode()
    {
        DataSetSearchCriterion criterion = new DataSetSearchCriterion();
        criterion.withType().withCode().thatEquals("LINK_TYPE");
        testSearch(TEST_USER, criterion, "20120628092259000-23", "20120628092259000-24", "20120628092259000-25");
    }

    @Test
    public void testSearchWithTypeWithPermId()
    {
        DataSetSearchCriterion criterion = new DataSetSearchCriterion();
        criterion.withType().withPermId().thatEquals("LINK_TYPE");
        testSearch(TEST_USER, criterion, "20120628092259000-23", "20120628092259000-24", "20120628092259000-25");
    }

    @Test
    public void testSearchWithAnyProperty()
    {
        DataSetSearchCriterion criterion = new DataSetSearchCriterion();
        criterion.withAnyProperty().thatEquals("non-virtual");
        testSearch(TEST_USER, criterion, "20110509092359990-11", "20110509092359990-12");
    }

    @Test
    public void testSearchWithAnyField()
    {
        DataSetSearchCriterion criterion = new DataSetSearchCriterion();
        criterion.withAnyField().thatEquals("20110509092359990-11");
        testSearch(TEST_USER, criterion, "20110509092359990-11");
    }

    @Test
    public void testSearchWithTag()
    {
        DataSetSearchCriterion criterion = new DataSetSearchCriterion();
        criterion.withTag().withId().thatEquals(new TagPermId("/test/TEST_METAPROJECTS"));
        testSearch(TEST_USER, criterion, "20120619092259000-22");
    }

    @Test
    public void testSearchWithRegistrationDate()
    {
        DataSetSearchCriterion criterion = new DataSetSearchCriterion();
        criterion.withRegistrationDate().thatEquals("2009-02-09");
        testSearch(TEST_USER, criterion, "20081105092159111-1", "20081105092159222-2", "20081105092159333-3", "20110805092359990-17");
    }

    @Test
    public void testSearchWithModificationDateThatEquals()
    {
        DataSetSearchCriterion criterion = new DataSetSearchCriterion();
        criterion.withModificationDate().thatEquals("2011-05-09");
        testSearch(TEST_USER, criterion, 14);
    }

    @Test
    public void testSearchWithAndOperator()
    {
        DataSetSearchCriterion criterion = new DataSetSearchCriterion();
        criterion.withAndOperator();
        criterion.withCode().thatContains("VALIDATIONS");
        criterion.withCode().thatContains("PARENT");
        testSearch(TEST_USER, criterion, "VALIDATIONS_PARENT-28");
    }

    @Test
    public void testSearchWithOrOperator()
    {
        DataSetSearchCriterion criterion = new DataSetSearchCriterion();
        criterion.withOrOperator();
        criterion.withPermId().thatEquals("20081105092159111-1");
        criterion.withPermId().thatEquals("20081105092159222-2");
        testSearch(TEST_USER, criterion, "20081105092159111-1", "20081105092159222-2");
    }

    @Test
    public void testSearchWithSpaceUnauthorized()
    {
        DataSetSearchCriterion criterion = new DataSetSearchCriterion();
        criterion.withPermId().thatEquals("20110509092359990-10");
        testSearch(TEST_USER, criterion, 1);

        criterion = new DataSetSearchCriterion();
        criterion.withPermId().thatEquals("20110509092359990-10");
        testSearch(TEST_SPACE_USER, criterion, 0);
    }

    private void testSearch(String user, DataSetSearchCriterion criterion, String... expectedIdentifiers)
    {
        List<DataSet> dataSets = searchDataSets(user, criterion, new DataSetFetchOptions());

        assertIdentifiers(dataSets, expectedIdentifiers);
    }

    private void testSearch(String user, DataSetSearchCriterion criterion, int expectedCount)
    {
        List<DataSet> dataSets = searchDataSets(user, criterion, new DataSetFetchOptions());
        assertEquals(dataSets.size(), expectedCount);
    }

    private List<DataSet> searchDataSets(String user, DataSetSearchCriterion criterion, DataSetFetchOptions fetchOptions)
    {
        String sessionToken = v3api.login(user, PASSWORD);
        SearchResult<DataSet> searchResult = v3api.searchDataSets(sessionToken, criterion, fetchOptions);
        List<DataSet> dataSets = searchResult.getObjects();
        v3api.logout(sessionToken);
        return dataSets;
    }

}
