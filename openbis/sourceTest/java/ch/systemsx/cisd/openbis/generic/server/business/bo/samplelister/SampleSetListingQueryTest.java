/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister;

import static ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.SampleListingTestUtils.asList;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleListingQuery;
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleSetListingQuery;
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleListingQuery.BaseSamplePropertyVO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleListingQuery.CoVoSamplePropertyVO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleListingQuery.GenericSamplePropertyVO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleListingQuery.MaterialSamplePropertyVO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleListingQuery.SampleRowVO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.AbstractDAOTest;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;

/**
 * Test cases for {@link ISampleSetListingQuery}.
 * 
 * @author Bernd Rinn
 */
@Test(groups =
    { "db", "sample" })
public class SampleSetListingQueryTest extends AbstractDAOTest
{

    private static final int CELL_PLATE_ID_CP_TEST_1 = 1042;

    private static final int CELL_PLATE_ID_CP_TEST_2 = 1043;

    private static final String SAMPLE_TYPE_CODE_MASTER_PLATE = "MASTER_PLATE";

    private static final String SAMPLE_TYPE_CODE_CELL_PLATE = "CELL_PLATE";

    private long dbInstanceId;

    private DatabaseInstancePE dbInstance;

    private GroupPE group;

    private LongSet masterPlateIds;

    private LongSet cellPlateIds;

    private ISampleSetListingQuery setQuery;

    private ISampleListingQuery query;

    @BeforeClass(alwaysRun = true)
    public void init()
    {
        dbInstanceId = daoFactory.getSampleListerDAO().getDatabaseInstanceId();
        dbInstance = daoFactory.getDatabaseInstanceDAO().getByTechId(new TechId(dbInstanceId));
        group = daoFactory.getGroupDAO().listGroups().get(0);
        final SampleTypePE masterPlateType =
                daoFactory.getSampleTypeDAO()
                        .tryFindSampleTypeByCode(SAMPLE_TYPE_CODE_MASTER_PLATE);
        final List<SamplePE> masterPlates =
                daoFactory.getSampleDAO().listSamplesWithPropertiesByTypeAndDatabaseInstance(
                        masterPlateType, dbInstance);
        masterPlateIds = new LongOpenHashSet(masterPlates.size());
        for (SamplePE sample : masterPlates)
        {
            masterPlateIds.add(sample.getId());
        }
        final SampleTypePE cellPlateType =
                daoFactory.getSampleTypeDAO().tryFindSampleTypeByCode(SAMPLE_TYPE_CODE_CELL_PLATE);
        final List<SamplePE> cellPlates =
                daoFactory.getSampleDAO().listSamplesWithPropertiesByTypeAndGroup(cellPlateType,
                        group);
        cellPlateIds = new LongOpenHashSet(cellPlates.size());
        for (SamplePE sample : cellPlates)
        {
            cellPlateIds.add(sample.getId());
        }
        setQuery = daoFactory.getSampleListerDAO().getIdSetQuery();
        query = daoFactory.getSampleListerDAO().getQuery();
    }

    private static LongSet createSet(long... values)
    {
        return new LongOpenHashSet(values);
    }

    @Test
    public void testQuerySamples()
    {
        LongSet ids = createSet(CELL_PLATE_ID_CP_TEST_1, CELL_PLATE_ID_CP_TEST_2);
        List<SampleRowVO> samples = asList(setQuery.getSamples(ids));
        assertEquals(ids.size(), samples.size());
        for (SampleRowVO sampleRowVO : samples)
        {
            assertTrue(ids.contains(sampleRowVO.id));
            SampleRowVO sameSample = query.getSample(sampleRowVO.id);
            assertTrue(EqualsBuilder.reflectionEquals(sampleRowVO, sameSample));
        }
    }

    @Test
    public void testSamplePropertyGenericValues()
    {
        LongSet ids = createSet(CELL_PLATE_ID_CP_TEST_1, CELL_PLATE_ID_CP_TEST_2);
        PropertyType propertyType =
                SampleListingTestUtils.findPropertyType(query.getPropertyTypes(), "comment");
        Iterable<GenericSamplePropertyVO> properties = setQuery.getSamplePropertyGenericValues(ids);
        List<GenericSamplePropertyVO> comments = findProperties(properties, propertyType.getId());
        assertEquals("There should be exactly one comment for each sample", ids.size(), comments
                .size());
        findExactlyOneProperty(comments, propertyType.getId(), CELL_PLATE_ID_CP_TEST_1);
        findExactlyOneProperty(comments, propertyType.getId(), CELL_PLATE_ID_CP_TEST_2);
        for (GenericSamplePropertyVO comment : comments)
        {
            if (comment.samp_id == CELL_PLATE_ID_CP_TEST_1)
            {
                assertEquals(comment.value, "very advanced stuff");
            } else if (comment.samp_id == CELL_PLATE_ID_CP_TEST_2)
            {
                assertEquals(comment.value, "extremely simple stuff");
            }
        }
    }

    private static <T extends BaseSamplePropertyVO> T findExactlyOneProperty(
            Iterable<T> properties, long propertyTypeId, long sampleId)
    {
        List<T> found = new ArrayList<T>();
        for (T property : properties)
        {
            if (property.prty_id == propertyTypeId && property.samp_id == sampleId)
            {
                found.add(property);
            }
        }
        if (found.size() != 1)
        {
            fail(String
                    .format(
                            "Exactly 1 property expected for sample id %d and property type id %d, but %d found.",
                            sampleId, propertyTypeId, found.size()));
        }
        return found.get(0);
    }

    private static <T extends BaseSamplePropertyVO> List<T> findProperties(Iterable<T> properties,
            long propertyTypeId)
    {
        List<T> found = new ArrayList<T>();
        for (T property : properties)
        {
            if (property.prty_id == propertyTypeId)
            {
                found.add(property);
            }
        }
        return found;
    }

    @Test
    public void testSamplePropertyMaterialValues()
    {
        LongSet ids = createSet(CELL_PLATE_ID_CP_TEST_1, CELL_PLATE_ID_CP_TEST_2);
        PropertyType[] propertyTypes = query.getPropertyTypes();
        List<MaterialSamplePropertyVO> allProperties =
                asList(setQuery.getSamplePropertyMaterialValues(ids));

        ensureSamplesHaveProperties("bacterium", propertyTypes, allProperties);
        ensureSamplesHaveProperties("any_material", propertyTypes, allProperties);
    }

    private List<MaterialSamplePropertyVO> ensureSamplesHaveProperties(String propertyCode,
            PropertyType[] propertyTypes, List<MaterialSamplePropertyVO> allProperties)
    {
        PropertyType propertyType =
                SampleListingTestUtils.findPropertyType(propertyTypes, propertyCode);
        List<MaterialSamplePropertyVO> properties =
                findProperties(allProperties, propertyType.getId());
        assertEquals("There should be exactly one property for each sample", 2, properties.size());
        findExactlyOneProperty(properties, propertyType.getId(), CELL_PLATE_ID_CP_TEST_1);
        findExactlyOneProperty(properties, propertyType.getId(), CELL_PLATE_ID_CP_TEST_2);
        return properties;
    }

    @Test
    // TODO 2009-08-17, Bernd Rinn: This test is a stub!
    public void testSamplePropertyVocabularyTermValues()
    {
        int sampleCount = 0;
        for (@SuppressWarnings("unused")
        CoVoSamplePropertyVO sampleProperty : setQuery
                .getSamplePropertyVocabularyTermValues(masterPlateIds))
        {
            ++sampleCount;
        }
        assertTrue(sampleCount > 0);
    }

}
