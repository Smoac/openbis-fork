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

package ch.ethz.sis.openbis.systemtest.asapi.v3;

import static org.junit.Assert.assertTrue;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.ArchivingStatus;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.Complete;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.TagPermId;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.openbis.systemtest.authorization.ProjectAuthorizationUser;

/**
 * @author pkupczyk
 */
public class SearchDataSetTest extends AbstractDataSetTest
{

    @Test
    public void testSearchWithEmptyCriteria()
    {
        testSearch(TEST_SPACE_USER, new DataSetSearchCriteria(), "20120619092259000-22", "20120628092259000-24", "20120628092259000-25",
                "20120628092259000-41", "VALIDATIONS_CNTNR-26", "VALIDATIONS_IMPOS-27", "VALIDATIONS_PARENT-28", "DATASET-TO-DELETE",
                "COMPONENT_3AX");
    }

    @Test
    public void testSearchWithIdSetToPermId()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withId().thatEquals(new DataSetPermId("20081105092259000-18"));
        testSearch(TEST_USER, criteria, "20081105092259000-18");
    }

    @Test
    public void testSearchWithPermId()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withPermId().thatEquals("20081105092259000-18");
        testSearch(TEST_USER, criteria, "20081105092259000-18");
    }

    @Test
    public void testSearchWithCode()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withCode().thatEquals("20081105092259000-18");
        testSearch(TEST_USER, criteria, "20081105092259000-18");
    }

    @Test
    public void testSearchWithCodes()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withCodes().thatIn(Arrays.asList("20081105092259000-18", "20081105092259000-19"));
        testSearch(TEST_USER, criteria, "20081105092259000-18", "20081105092259000-19");
    }
    
    @Test
    public void testSearchTwoDataSetsWithCodeAndId()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withOrOperator();
        criteria.withCode().thatEquals("20081105092259000-18");
        criteria.withCode().thatEquals("20081105092259000-19");
        testSearch(TEST_USER, criteria, "20081105092259000-18", "20081105092259000-19");
    }

    @Test
    public void testSearchWithProperty()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withProperty("COMMENT").thatContains("non-virt");
        testSearch(TEST_USER, criteria, "20110509092359990-11", "20110509092359990-12");
    }

    @Test
    public void testSearchWithRegistrationDateIsEarlierThan()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withRegistrationDate().thatIsEarlierThanOrEqualTo("2008-11-05 09:22:00");
        testSearch(TEST_USER, criteria, "20081105092159188-3");
    }

    @Test
    public void testSearchWithModificationDateIsLaterThan()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withModificationDate().thatIsLaterThanOrEqualTo("2011-05-01");
        criteria.withContainer().withCode().thatContains("2");
        testSearch(TEST_USER, criteria, "20110509092359990-11", "COMPONENT_2A", "20110509092359990-12");
    }

    @Test
    public void testSearchWithContainer()
    {
        final DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withContainer();
        testSearch(TEST_USER, criteria, "CONTAINER_1", "CONTAINER_2", "20110509092359990-11", "20110509092359990-12",
                "VALIDATIONS_IMPOS-27", "COMPONENT_1A", "COMPONENT_2A", "COMPONENT_1B", "COMPONENT_3A", "COMPONENT_3AB",
                "COMPONENT_3AX");
    }

    @Test
    public void testSearchWithContainerWithPermId()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withContainer().withPermId().thatEquals("20110509092359990-10");
        testSearch(TEST_USER, criteria, "20110509092359990-11", "20110509092359990-12");
    }

    @Test
    public void testSearchWithChildren()
    {
        final DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withChildren();
        testSearch(TEST_USER, criteria, "20081105092159111-1", "20081105092159222-2", "20081105092159333-3",
                "20081105092259000-19", "20081105092259000-9", "20081105092259900-0", "20081105092259900-1",
                "20110805092359990-17", "VALIDATIONS_PARENT-28");
    }

    @Test
    public void testSearchWithChildrenWithCode()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withChildren().withCode().thatEquals("20081105092259000-9");
        testSearch(TEST_USER, criteria, "20081105092159111-1", "20081105092159222-2", "20081105092159333-3");
    }

    @Test
    public void testSearchWithChildrenWithPropertyEquals()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withProperty("GENDER").thatEquals("FEMALE");
        criteria.withChildren().withCode().thatEquals("20081105092259000-9");
        testSearch(TEST_USER, criteria, "20081105092159111-1");
    }

    @Test
    public void testSearchWithParent()
    {
        final DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withParents();
        testSearch(TEST_USER, criteria, "20081105092259000-18", "20081105092259000-20", "20081105092259000-21",
                "20081105092259000-8", "20081105092259000-9", "20081105092259900-0", "20081105092259900-1",
                "20081105092359990-2", "VALIDATIONS_IMPOS-27");
    }

    @Test
    public void testSearchWithParentWithCode()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withParents().withCode().thatEquals("20081105092159111-1");
        testSearch(TEST_USER, criteria, "20081105092259000-9");
    }

    @Test
    public void testSearchWithExperiment()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withCode().thatStartsWith("20120628092259000");
        criteria.withExperiment();
        testSearch(TEST_USER, criteria, "20120628092259000-24", "20120628092259000-25", "20120628092259000-41");
    }

    @Test
    public void testSearchWithoutExperiment()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withoutExperiment();
        testSearch(TEST_USER, criteria, "20120628092259000-23");
    }

    @Test
    public void testSearchWithExperimentWithPermIdThatEquals()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withExperiment().withPermId().thatEquals("200902091255058-1035");
        testSearch(TEST_USER, criteria, "20081105092159333-3", "20110805092359990-17");
    }

    @Test
    public void testSearchWithExperimentWithProperty()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withExperiment().withProperty("GENDER");
        testSearch(TEST_USER, criteria, "20081105092159333-3", "20110805092359990-17", "20081105092159188-3");
    }

    @Test
    public void testSearchWithExperimentWithPropertyThatEquals()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withExperiment().withProperty("GENDER").thatEquals("MALE");
        testSearch(TEST_USER, criteria, "20081105092159188-3");
    }

    @Test
    public void testSearchWithExperimentYoungerThan()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withExperiment().withRegistrationDate().thatIsLaterThanOrEqualTo("2009-02-09 12:11:00");
        testSearch(TEST_USER, criteria, "20081105092159333-3", "20110805092359990-17");
    }

    @Test
    public void testSearchWithSample()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withCode().thatStartsWith("20120628092259000");
        criteria.withSample();
        testSearch(TEST_USER, criteria, "20120628092259000-23", "20120628092259000-41");
    }

    @Test
    public void testSearchWithoutSample()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withCode().thatStartsWith("20120628092259000");
        criteria.withoutSample();
        testSearch(TEST_USER, criteria, "20120628092259000-24", "20120628092259000-25");
    }

    @Test
    public void testSearchWithSampleWithPropertiesThatContains()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        SampleSearchCriteria sampleSearchCriteria = criteria.withSample().withOrOperator();
        sampleSearchCriteria.withProperty("BACTERIUM").thatContains("M-X");
        sampleSearchCriteria.withProperty("ORGANISM").thatContains("LY");
        testSearch(TEST_USER, criteria, "20081105092159111-1", "20081105092159333-3", "20110805092359990-17", "20120628092259000-41");
    }

    @Test
    public void testSearchWithTypeWithIdSetToPermId()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withType().withId().thatEquals(new EntityTypePermId("LINK_TYPE"));
        testSearch(TEST_USER, criteria, "20120628092259000-23", "20120628092259000-24", "20120628092259000-25", "20120628092259000-41");
    }

    @Test
    public void testSearchWithTypeWithCode()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withType().withCode().thatEquals("LINK_TYPE");
        testSearch(TEST_USER, criteria, "20120628092259000-23", "20120628092259000-24", "20120628092259000-25", "20120628092259000-41");
    }

    @Test
    public void testSearchWithTypeWithPermId()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withType().withPermId().thatEquals("LINK_TYPE");
        testSearch(TEST_USER, criteria, "20120628092259000-23", "20120628092259000-24", "20120628092259000-25", "20120628092259000-41");
    }

    @Test
    public void testSearchWithAnyProperty()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withAnyProperty().thatStartsWith("non-virtual");
        testSearch(TEST_USER, criteria, "20110509092359990-11", "20110509092359990-12");
    }

    @Test
    public void testSearchWithAnyField()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withAnyField().thatEquals("20110509092359990-11");
        testSearch(TEST_USER, criteria, "20110509092359990-11");
    }

    @Test
    public void testSearchWithAnyFieldMatchingSampleProperty()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        final PropertyTypePermId propertyType = createASamplePropertyType(sessionToken, null);

        final EntityTypePermId dataSetType = createADataSetType(sessionToken, false, propertyType);
        final DataSetCreation dataSetCreation = physicalDataSetCreation();
        dataSetCreation.setTypeId(dataSetType);
        dataSetCreation.setExperimentId(new ExperimentIdentifier("/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST"));
        dataSetCreation.setProperty(propertyType.getPermId(), "/CISD/CL1");
        v3api.createDataSets(sessionToken, Arrays.asList(dataSetCreation));

        final DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withOrOperator();
        criteria.withAnyField().thatEquals("/CISD/CL1");

        testSearch(TEST_USER, criteria, 1);

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithAnyPropertyMatchingSampleProperty()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        final PropertyTypePermId propertyType = createASamplePropertyType(sessionToken, null);

        final EntityTypePermId dataSetType = createADataSetType(sessionToken, false, propertyType);
        final DataSetCreation dataSetCreation = physicalDataSetCreation();
        dataSetCreation.setTypeId(dataSetType);
        dataSetCreation.setExperimentId(new ExperimentIdentifier("/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST"));
        dataSetCreation.setProperty(propertyType.getPermId(), "/CISD/CL1");
        v3api.createDataSets(sessionToken, Arrays.asList(dataSetCreation));

        final DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withOrOperator();
        criteria.withAnyProperty().thatEquals("/CISD/CL1");

        testSearch(TEST_USER, criteria, 1);

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithTag()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withTag().withId().thatEquals(new TagPermId("/test/TEST_METAPROJECTS"));
        testSearch(TEST_USER, criteria, "20120619092259000-22");
    }

    @Test
    public void testSearchWithPhysicalData()
    {
        final DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData();
        testSearch(TEST_USER, criteria, 25);
    }

    @Test
    public void testSearchWithPhysicalDataWithShareIdThatEquals()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withShareId().thatEquals("42");
        testSearch(TEST_USER, criteria, "20081105092159111-1");

        criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withShareId().thatEquals("4");
        testSearch(TEST_USER, criteria);

        criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withShareId().thatEquals("2");
        testSearch(TEST_USER, criteria);
    }

    @Test
    public void testSearchWithPhysicalDataWithShareIdThatStartsWith()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withShareId().thatStartsWith("42");
        testSearch(TEST_USER, criteria, "20081105092159111-1");

        criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withShareId().thatStartsWith("4");
        testSearch(TEST_USER, criteria, "20081105092159111-1");

        criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withShareId().thatStartsWith("2");
        testSearch(TEST_USER, criteria);
    }

    @Test
    public void testSearchWithPhysicalDataWithShareIdThatEndsWith()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withShareId().thatEndsWith("42");
        testSearch(TEST_USER, criteria, "20081105092159111-1");

        criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withShareId().thatEndsWith("4");
        testSearch(TEST_USER, criteria);

        criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withShareId().thatEndsWith("2");
        testSearch(TEST_USER, criteria, "20081105092159111-1");
    }

    @Test
    public void testSearchWithPhysicalDataWithLocationThatEquals()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withLocation().thatEquals("analysis/result");
        testSearch(TEST_USER, criteria, "20081105092159188-3");

        criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withLocation().thatEquals("analys");
        testSearch(TEST_USER, criteria);

        criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withLocation().thatEquals("result");
        testSearch(TEST_USER, criteria);
    }

    @Test
    public void testSearchWithPhysicalDataWithLocationThatStartsWith()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withLocation().thatStartsWith("analysis/result");
        testSearch(TEST_USER, criteria, "20081105092159188-3");

        criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withLocation().thatStartsWith("analys");
        testSearch(TEST_USER, criteria, "20081105092159188-3");

        criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withLocation().thatStartsWith("result");
        testSearch(TEST_USER, criteria);
    }

    @Test
    public void testSearchWithPhysicalDataWithLocationThatEndsWith()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withLocation().thatEndsWith("analysis/result");
        testSearch(TEST_USER, criteria, "20081105092159188-3");

        criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withLocation().thatEndsWith("analys");
        testSearch(TEST_USER, criteria);

        criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withLocation().thatEndsWith("result");
        testSearch(TEST_USER, criteria, "20081105092159188-3");
    }

    @Test
    public void testSearchWithPhysicalDataWithSizeThatEquals()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withSize().thatEquals(4710);
        testSearch(TEST_USER, criteria);

        criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withSize().thatEquals(4711);
        testSearch(TEST_USER, criteria, "20081105092159111-1");

        criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withSize().thatEquals(4712);
        testSearch(TEST_USER, criteria);
    }

    @Test
    public void testSearchWithPhysicalDataWithSizeThatIsGreaterThan()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withSize().thatIsGreaterThan(4710);
        testSearch(TEST_USER, criteria, "20081105092159111-1");

        criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withSize().thatIsGreaterThan(4711);
        testSearch(TEST_USER, criteria);

        criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withSize().thatIsGreaterThan(4712);
        testSearch(TEST_USER, criteria);
    }

    @Test
    public void testSearchWithPhysicalDataWithSizeThatIsLessThan()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withSize().thatIsLessThan(4710);
        testSearch(TEST_USER, criteria);

        criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withSize().thatIsLessThan(4711);
        testSearch(TEST_USER, criteria);

        criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withSize().thatIsLessThan(4712);
        testSearch(TEST_USER, criteria, "20081105092159111-1");
    }

    @Test
    public void testSearchWithPhysicalDataWithStorageFormatWithCodeThatEquals()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withStorageFormat().withCode().thatEquals("PROPRIETARY");
        testSearch(TEST_USER, criteria, 25);

        criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withStorageFormat().withCode().thatEquals("PRO");
        testSearch(TEST_USER, criteria);

        criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withStorageFormat().withCode().thatEquals("TARY");
        testSearch(TEST_USER, criteria);
    }

    @Test
    public void testSearchWithPhysicalDataWithFileFormatTypeWithCodeThatEquals()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withFileFormatType().withCode().thatEquals("TIFF");
        testSearch(TEST_USER, criteria, "20081105092159111-1");

        criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withFileFormatType().withCode().thatEquals("TIF");
        testSearch(TEST_USER, criteria);

        criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withFileFormatType().withCode().thatEquals("IFF");
        testSearch(TEST_USER, criteria);
    }

    @Test
    public void testSearchWithPhysicalDataWithLocatorTypeWithCodeThatEquals()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withLocatorType().withCode().thatEquals("RELATIVE_LOCATION");
        testSearch(TEST_USER, criteria, 25);

        criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withLocatorType().withCode().thatEquals("REL");
        testSearch(TEST_USER, criteria);

        criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withLocatorType().withCode().thatEquals("TION");
        testSearch(TEST_USER, criteria);
    }

    @Test
    public void testSearchWithPhysicalDataWithCompleteThatEquals()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withComplete().thatEquals(Complete.UNKNOWN);
        testSearch(TEST_USER, criteria, 25);

        criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withComplete().thatEquals(Complete.YES);
        testSearch(TEST_USER, criteria);

        criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withComplete().thatEquals(Complete.NO);
        testSearch(TEST_USER, criteria);
    }

    @Test
    public void testSearchWithPhysicalDataWithStatusThatEquals()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withStatus().thatEquals(ArchivingStatus.AVAILABLE);
        testSearch(TEST_USER, criteria, 25);

        criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withStatus().thatEquals(ArchivingStatus.ARCHIVED);
        testSearch(TEST_USER, criteria);
    }

    @Test
    public void testSearchWithPhysicalDataWithPresentInArchiveThatEquals()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withPresentInArchive().thatEquals(false);
        testSearch(TEST_USER, criteria, 25);

        criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withPresentInArchive().thatEquals(true);
        testSearch(TEST_USER, criteria);
    }

    @Test
    public void testSearchWithPhysicalDataWithArchivingRequestedThatEquals()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withArchivingRequested().thatEquals(true);
        testSearch(TEST_USER, criteria, "20081105092159188-3");

        criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withArchivingRequested().thatEquals(false);
        testSearch(TEST_USER, criteria, 24);
    }

    @Test
    public void testSearchWithPhysicalDataWithStorageConfirmationThatEquals()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withStorageConfirmation().thatEquals(false);
        testSearch(TEST_USER, criteria, "20081105092159111-1");

        criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withStorageConfirmation().thatEquals(true);
        testSearch(TEST_USER, criteria, 24);
    }

    @Test
    public void testSearchWithPhysicalDataWithSpeedHintThatEquals()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withSpeedHint().thatEquals(-50);
        testSearch(TEST_USER, criteria, 24);

        criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withSpeedHint().thatEquals(42);
        testSearch(TEST_USER, criteria, "20081105092159111-1");

        criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withSpeedHint().thatEquals(50);
        testSearch(TEST_USER, criteria);
    }

    @Test
    public void testSearchWithPhysicalDataWithSpeedHintThatIsGreaterThan()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withSpeedHint().thatIsGreaterThan(-50);
        testSearch(TEST_USER, criteria, "20081105092159111-1");

        criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withSpeedHint().thatIsGreaterThan(42);
        testSearch(TEST_USER, criteria);

        criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withSpeedHint().thatIsGreaterThan(50);
        testSearch(TEST_USER, criteria);
    }

    @Test
    public void testSearchWithPhysicalDataWithSpeedHintThatIsLessThan()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withSpeedHint().thatIsLessThan(-50);
        testSearch(TEST_USER, criteria);

        criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withSpeedHint().thatIsLessThan(42);
        testSearch(TEST_USER, criteria, 24);

        criteria = new DataSetSearchCriteria();
        criteria.withPhysicalData().withSpeedHint().thatIsLessThan(50);
        testSearch(TEST_USER, criteria, 25);
    }

    @Test
    public void testSearchWithLinkedData()
    {
        final DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withLinkedData();
        testSearch(TEST_USER, criteria, 4);
    }

    @Test
    public void testSearchWithLinkedDataWithExternalCodeThatEquals()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withLinkedData().withExternalCode().thatEquals("CODE1");
        testSearch(TEST_USER, criteria, "20120628092259000-23");

        criteria = new DataSetSearchCriteria();
        criteria.withLinkedData().withExternalCode().thatEquals("CODE");
        testSearch(TEST_USER, criteria);

        criteria = new DataSetSearchCriteria();
        criteria.withLinkedData().withExternalCode().thatEquals("ODE1");
        testSearch(TEST_USER, criteria);
    }

    @Test
    public void testSearchWithLinkedDataWithExternalCodeThatStartsWith()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withLinkedData().withExternalCode().thatStartsWith("CODE1");
        testSearch(TEST_USER, criteria, "20120628092259000-23");

        criteria = new DataSetSearchCriteria();
        criteria.withLinkedData().withExternalCode().thatStartsWith("CODE");
        testSearch(TEST_USER, criteria, "20120628092259000-23", "20120628092259000-24", "20120628092259000-25");

        criteria = new DataSetSearchCriteria();
        criteria.withLinkedData().withExternalCode().thatStartsWith("ODE1");
        testSearch(TEST_USER, criteria);
    }

    @Test
    public void testSearchWithLinkedDataWithExternalCodeThatEndsWith()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withLinkedData().withExternalCode().thatEndsWith("CODE1");
        testSearch(TEST_USER, criteria, "20120628092259000-23");

        criteria = new DataSetSearchCriteria();
        criteria.withLinkedData().withExternalCode().thatEndsWith("CODE");
        testSearch(TEST_USER, criteria);

        criteria = new DataSetSearchCriteria();
        criteria.withLinkedData().withExternalCode().thatEndsWith("ODE1");
        testSearch(TEST_USER, criteria, "20120628092259000-23");
    }

    @Test
    public void testSearchWithLinkedDataWithExternalDmsWithCodeThatEquals()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withLinkedData().withExternalDms().withCode().thatEquals("DMS_1");
        testSearch(TEST_USER, criteria, "20120628092259000-23", "20120628092259000-24");

        criteria = new DataSetSearchCriteria();
        criteria.withLinkedData().withExternalDms().withCode().thatEquals("DM");
        testSearch(TEST_USER, criteria);

        criteria = new DataSetSearchCriteria();
        criteria.withLinkedData().withExternalDms().withCode().thatEquals("MS");
        testSearch(TEST_USER, criteria);
    }

    @Test
    public void testSearchWithRegistratorWithUserIdThatEquals()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withRegistrator().withUserId().thatEquals("etlserver");
        testSearch(TEST_USER, criteria, "20081105092259900-1", "20081105092359990-2");
    }

    @Test
    public void testSearchWithRegistratorWithFirstNameThatEquals()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withRegistrator().withFirstName().thatEquals("John 2");
        testSearch(TEST_USER, criteria, "20081105092259900-1", "20081105092359990-2");
    }

    @Test
    public void testSearchWithRegistratorWithLastNameThatEquals()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withRegistrator().withLastName().thatEquals("ETL Server");
        testSearch(TEST_USER, criteria, "20081105092259900-1", "20081105092359990-2");
    }

    @Test
    public void testSearchWithRegistratorWithEmailThatEquals()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withRegistrator().withEmail().thatEquals("etlserver@systemsx.ch");
        testSearch(TEST_USER, criteria, "20081105092259900-1", "20081105092359990-2");
    }

    @Test
    public void testSearchWithModifierWithUserIdThatEquals()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withModifier().withUserId().thatEquals("etlserver");
        testSearch(TEST_USER, criteria, "20110509092359990-11", "20110509092359990-12");
    }

    @Test
    public void testSearchWithModifierWithFirstNameThatEquals()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withModifier().withFirstName().thatEquals("John 2");
        testSearch(TEST_USER, criteria, "20110509092359990-11", "20110509092359990-12");
    }

    @Test
    public void testSearchWithModifierWithLastNameThatEquals()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withModifier().withLastName().thatEquals("ETL Server");
        testSearch(TEST_USER, criteria, "20110509092359990-11", "20110509092359990-12");
    }

    @Test
    public void testSearchWithModifierWithEmailThatEquals()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withModifier().withEmail().thatEquals("etlserver@systemsx.ch");
        testSearch(TEST_USER, criteria, "20110509092359990-11", "20110509092359990-12");
    }

    @Test
    public void testSearchWithRegistrationDateThatEquals()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withRegistrationDate().thatEquals("2009-02-09");
        testSearch(TEST_USER, criteria, "20081105092159111-1", "20081105092159222-2", "20081105092159333-3", "20110805092359990-17");
    }

    @Test
    public void testSearchWithModificationDateThatEquals()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withModificationDate().thatEquals("2011-05-09");
        testSearch(TEST_USER, criteria, 14);
    }

    @Test
    public void testSearchWithAndOperator()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withAndOperator();
        criteria.withCode().thatContains("VALIDATIONS");
        criteria.withCode().thatContains("PARENT");
        testSearch(TEST_USER, criteria, "VALIDATIONS_PARENT-28");
    }

    @Test
    public void testSearchWithOrOperator()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withOrOperator();
        criteria.withPermId().thatEquals("20081105092159111-1");
        criteria.withPermId().thatEquals("20081105092159222-2");
        testSearch(TEST_USER, criteria, "20081105092159111-1", "20081105092159222-2");
    }

    @Test
    public void testSearchWithSpaceUnauthorized()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withPermId().thatEquals("20110509092359990-10");
        testSearch(TEST_USER, criteria, 1);

        criteria = new DataSetSearchCriteria();
        criteria.withPermId().thatEquals("20110509092359990-10");
        testSearch(TEST_SPACE_USER, criteria, 0);
    }

    @Test
    public void testSearchWithSortingByProperty()
    {
        final DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withOrOperator();
        criteria.withPermId().thatEquals("20110509092359990-12");
        criteria.withPermId().thatEquals("20081105092259000-20");
        criteria.withPermId().thatEquals("20081105092159111-1");

        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withProperties();

        fo.sortBy().property("$COMMENT").asc();
        final List<DataSet> dataSets1 = v3api.searchDataSets(sessionToken, criteria, fo).getObjects();
        assertDataSetCodesInOrder(dataSets1, "20081105092259000-20", "20081105092159111-1", "20110509092359990-12");

        fo.sortBy().property("$COMMENT").desc();
        fo.from(0).count(3);
        final List<DataSet> dataSets2 = v3api.searchDataSets(sessionToken, criteria, fo).getObjects();
        assertDataSetCodesInOrder(dataSets2, "20110509092359990-12", "20081105092159111-1", "20081105092259000-20");

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithAndSortingByPropertyAndPaging()
    {
        final DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withOrOperator();
        criteria.withPermId().thatEquals("20110509092359990-12");
        criteria.withPermId().thatEquals("20081105092259000-20");
        criteria.withPermId().thatEquals("20081105092159111-1");

        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withProperties();

        fo.sortBy().property("$COMMENT").asc();
        fo.from(1).count(2);
        final SearchResult<DataSet> dataSetSearchResult1 = v3api.searchDataSets(sessionToken, criteria, fo);
        assertEquals(dataSetSearchResult1.getTotalCount(), 3);
        List<DataSet> dataSets1 = dataSetSearchResult1.getObjects();
        assertDataSetCodesInOrder(dataSets1, "20081105092159111-1", "20110509092359990-12");

        fo.sortBy().property("$COMMENT").asc();
        fo.from(1).count(1);
        final SearchResult<DataSet> dataSetSearchResult2 = v3api.searchDataSets(sessionToken, criteria, fo);
        assertEquals(dataSetSearchResult2.getTotalCount(), 3);
        List<DataSet> dataSets2 = dataSetSearchResult2.getObjects();
        assertDataSetCodesInOrder(dataSets2, "20081105092159111-1");

        fo.sortBy().property("$COMMENT").desc();
        fo.from(1).count(3);
        final SearchResult<DataSet> dataSetSearchResult3 = v3api.searchDataSets(sessionToken, criteria, fo);
        assertEquals(dataSetSearchResult3.getTotalCount(), 3);
        List<DataSet> dataSets3 = dataSetSearchResult3.getObjects();
        assertDataSetCodesInOrder(dataSets3, "20081105092159111-1", "20081105092259000-20");

        fo.sortBy().property("$COMMENT").desc();
        fo.from(2).count(1);
        final SearchResult<DataSet> dataSetSearchResult4 = v3api.searchDataSets(sessionToken, criteria, fo);
        assertEquals(dataSetSearchResult4.getTotalCount(), 3);
        List<DataSet> dataSets4 = dataSetSearchResult4.getObjects();
        assertDataSetCodesInOrder(dataSets4, "20081105092259000-20");

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithSortingByCode()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withOrOperator();
        criteria.withId().thatEquals(new DataSetPermId("COMPONENT_1B"));
        criteria.withId().thatEquals(new DataSetPermId("COMPONENT_1A"));
        criteria.withId().thatEquals(new DataSetPermId("COMPONENT_2A"));

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetFetchOptions fo = new DataSetFetchOptions();

        fo.sortBy().code().asc();
        List<DataSet> dataSets1 = v3api.searchDataSets(sessionToken, criteria, fo).getObjects();
        assertDataSetCodes(dataSets1, "COMPONENT_1A", "COMPONENT_1B", "COMPONENT_2A");

        fo.sortBy().code().desc();
        List<DataSet> dataSets2 = v3api.searchDataSets(sessionToken, criteria, fo).getObjects();
        assertDataSetCodes(dataSets2, "COMPONENT_2A", "COMPONENT_1B", "COMPONENT_1A");

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithSortingByType()
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withOrOperator();
        criteria.withId().thatEquals(new DataSetPermId("ROOT_CONTAINER"));
        criteria.withId().thatEquals(new DataSetPermId("COMPONENT_1A"));
        criteria.withId().thatEquals(new DataSetPermId("COMPONENT_2A"));

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withType();

        fo.sortBy().type().asc();
        fo.sortBy().code().asc();
        List<DataSet> dataSets1 = v3api.searchDataSets(sessionToken, criteria, fo).getObjects();
        assertDataSetCodes(dataSets1, "ROOT_CONTAINER", "COMPONENT_1A", "COMPONENT_2A");

        fo.sortBy().type().desc();
        fo.sortBy().code().desc();
        List<DataSet> dataSets2 = v3api.searchDataSets(sessionToken, criteria, fo).getObjects();
        assertDataSetCodes(dataSets2, "COMPONENT_2A", "COMPONENT_1A", "ROOT_CONTAINER");

        v3api.logout(sessionToken);
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testSearchWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withOrOperator();
        criteria.withId().thatEquals(new DataSetPermId("20120619092259000-22"));
        criteria.withId().thatEquals(new DataSetPermId("20081105092159188-3"));

        String sessionToken = v3api.login(user.getUserId(), PASSWORD);

        SearchResult<DataSet> result = v3api.searchDataSets(sessionToken, criteria, new DataSetFetchOptions());

        if (user.isInstanceUser())
        {
            assertEquals(result.getObjects().size(), 2);
        } else if ((user.isTestSpaceUser() || user.isTestProjectUser()) && !user.isDisabledProjectUser())
        {
            assertEquals(result.getObjects().size(), 1);
            assertEquals(result.getObjects().get(0).getCode(), "20120619092259000-22");
        } else
        {
            assertEquals(result.getObjects().size(), 0);
        }

        v3api.logout(sessionToken);
    }

    @Test
    public void testFetchDataSetKind()
    {
        // given
        DataSetSearchCriteria criteria = new DataSetSearchCriteria();
        criteria.withOrOperator();
        criteria.withId().thatEquals(new DataSetPermId("20120628092259000-23")); // link
        criteria.withId().thatEquals(new DataSetPermId("COMPONENT_1A")); // physical
        criteria.withId().thatEquals(new DataSetPermId("ROOT_CONTAINER")); // container

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.sortBy().code().asc();

        // when
        List<DataSet> dataSets = v3api.searchDataSets(sessionToken, criteria, fo).getObjects();

        // then
        assertEquals(dataSets.get(0).getKind(), DataSetKind.LINK);
        assertEquals(dataSets.get(1).getKind(), DataSetKind.PHYSICAL);
        assertEquals(dataSets.get(2).getKind(), DataSetKind.CONTAINER);

        v3api.logout(sessionToken);
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetSearchCriteria c = new DataSetSearchCriteria();
        c.withCode().thatEquals("20081105092259000-18");

        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withSample();
        fo.withExperiment();

        v3api.searchDataSets(sessionToken, c, fo);

        assertAccessLog(
                "search-data-sets  SEARCH_CRITERIA:\n'DATASET\n    with attribute 'code' equal to '20081105092259000-18'\n'\nFETCH_OPTIONS:\n'DataSet\n    with Experiment\n    with Sample\n'");
    }

    private void testSearch(String user, DataSetSearchCriteria criteria, String... expectedIdentifiers)
    {
        List<DataSet> dataSets = searchDataSets(user, criteria, new DataSetFetchOptions());

        assertIdentifiers(dataSets, expectedIdentifiers);
    }

    private void testSearch(String user, DataSetSearchCriteria criteria, int expectedCount)
    {
        List<DataSet> dataSets = searchDataSets(user, criteria, new DataSetFetchOptions());
        assertEquals(dataSets.size(), expectedCount);
    }

    private List<DataSet> searchDataSets(String user, DataSetSearchCriteria criteria, DataSetFetchOptions fetchOptions)
    {
        String sessionToken = v3api.login(user, PASSWORD);
        SearchResult<DataSet> searchResult = v3api.searchDataSets(sessionToken, criteria, fetchOptions);
        List<DataSet> dataSets = searchResult.getObjects();
        v3api.logout(sessionToken);
        return dataSets;
    }

}
