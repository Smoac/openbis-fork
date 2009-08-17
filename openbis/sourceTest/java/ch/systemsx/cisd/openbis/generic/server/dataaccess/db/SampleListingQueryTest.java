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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.ISampleListingQuery.SampleRowVO;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * Test cases for {@link ISampleListingQuery}.
 * 
 * @author Bernd Rinn
 */
@Test(groups =
    { "db", "sample" })
public class SampleListingQueryTest extends AbstractDAOTest
{

    private static final String SAMPLE_TYPE_CODE_MASTER_PLATE = "MASTER_PLATE";

    private long dbInstanceId;

    private DatabaseInstancePE dbInstance;

    private GroupPE group;

    private long groupId;

    private String groupCode;

    private SampleTypePE masterPlateType;

    private SamplePE firstMasterPlate;

    private ExperimentPE firstExperiment;

    private PersonPE firstPerson;

    private ISampleListingQuery query;

    @BeforeClass(alwaysRun = true)
    public void init()
    {
        dbInstanceId = daoFactory.getSampleListerDAO().getDatabaseInstanceId();
        dbInstance = daoFactory.getDatabaseInstanceDAO().getByTechId(new TechId(dbInstanceId));
        group = daoFactory.getGroupDAO().listGroups().get(0);
        groupId = group.getId();
        groupCode = group.getCode();
        masterPlateType =
                daoFactory.getSampleTypeDAO()
                        .tryFindSampleTypeByCode(SAMPLE_TYPE_CODE_MASTER_PLATE);
        firstMasterPlate =
                daoFactory.getSampleDAO().listSamplesWithPropertiesByTypeAndDatabaseInstance(
                        masterPlateType, dbInstance).get(0);
        firstExperiment = daoFactory.getExperimentDAO().listExperiments().get(0);
        firstPerson = daoFactory.getPersonDAO().getPerson(1);
        query = daoFactory.getSampleListerDAO().getQuery();
    }

    private Map<Long, SamplePE> createIdMap(final List<SamplePE> sampleList, boolean dropNonListable)
    {
        final Map<Long, SamplePE> sampleMap = new HashMap<Long, SamplePE>();
        for (SamplePE sample : sampleList)
        {
            if (dropNonListable && sample.getSampleType().isListable() == false)
            {
                continue;
            }
            sampleMap.put(sample.getId(), sample);
        }
        return sampleMap;
    }

    @Test(groups = "slow")
    public void testQuerySamples()
    {
        final long listableSamplesInTestDB = query.getSampleCount();
        assertTrue(listableSamplesInTestDB > 0);
        int sampleCount = 0;
        for (SampleRowVO sample : query.getSamples())
        {
            final String msg = "id: " + sample.id;
            final SampleRowVO sample2 = query.getSample(sample.id);
            assertTrue(msg, EqualsBuilder.reflectionEquals(sample, sample2));
            final SamplePE samplePE = daoFactory.getSampleDAO().getByTechId(new TechId(sample.id));
            assertEquals(msg, samplePE.getCode(), sample.code);
            assertEquals(msg, samplePE.getPermId(), sample.perm_id);
            assertEquals(msg, samplePE.getRegistrator().getId().longValue(),
                    sample.pers_id_registerer);
            assertEquals(msg, samplePE.getRegistrationDate(), sample.registration_timestamp);
            assertEquals(msg, samplePE.getSampleType().getId().longValue(), sample.saty_id);
            if (samplePE.getExperiment() == null)
            {
                assertNull(msg, sample.expe_id);
            } else
            {
                assertEquals(msg, samplePE.getExperiment().getId(), sample.expe_id);
            }
            if (samplePE.getInvalidation() == null)
            {
                assertNull(msg, sample.inva_id);
            } else
            {
                assertEquals(msg, samplePE.getInvalidation().getId(), sample.inva_id);
            }
            if (samplePE.getGeneratedFrom() == null)
            {
                assertNull(msg, sample.samp_id_generated_from);

            } else
            {
                // Work around Hibernate peculiarity
                Long idGeneratedFrom = samplePE.getGeneratedFrom().getId();
                if (idGeneratedFrom == null)
                {
                    idGeneratedFrom = HibernateUtils.getId(samplePE.getGeneratedFrom());
                }
                assertEquals(msg, idGeneratedFrom, sample.samp_id_generated_from);
            }
            if (samplePE.getContainer() == null)
            {
                assertNull(msg, sample.samp_id_part_of);
            } else
            {
                assertNotNull(msg, samplePE.getContainer().getId());
                assertEquals(msg, samplePE.getContainer().getId(), sample.samp_id_part_of);
            }
            ++sampleCount;
        }
        assertEquals(listableSamplesInTestDB, sampleCount);
    }

    @Test
    public void testQueryGroupSamples()
    {
        final Map<Long, SamplePE> sampleMap =
                createIdMap(daoFactory.getSampleDAO().listSamplesWithPropertiesByGroup(group), true);
        assertFalse(sampleMap.isEmpty());

        int sampleCount = 0;
        for (SampleRowVO sample : query.getGroupSamples(dbInstanceId, groupCode))
        {
            final String msg = "id: " + sample.id;
            final SampleRowVO sample2 = query.getSample(sample.id);
            assertTrue(msg, EqualsBuilder.reflectionEquals(sample, sample2));
            // We have to go the d-tour via samplePE as the sample doesn't contain the group id,
            // and no, we don't want to add it just for the test
            final SamplePE samplePE = sampleMap.get(sample.id);
            assertEquals(msg, groupId, samplePE.getGroup().getId().longValue());
            ++sampleCount;
        }
        assertEquals(sampleMap.size(), sampleCount);
    }

    @Test
    // TODO 2009-08-17, Bernd Rinn: This test is a stub!
    public void testQueryGroupSamplesBySampleType()
    {
        int sampleCount = 0;
        for (@SuppressWarnings("unused")
        SampleRowVO sample : query.getGroupSamplesForSampleType(dbInstanceId, groupCode,
                masterPlateType.getId()))
        {
            // final String msg = "id: " + sample.id;
            ++sampleCount;
        }
        assertTrue(sampleCount > 0);
    }

    @Test
    public void testQueryExperimentSamples()
    {
        final ExperimentPE experiment = daoFactory.getExperimentDAO().listExperiments().get(0);
        final long experimentId = experiment.getId();
        final Map<Long, SamplePE> sampleMap =
                createIdMap(daoFactory.getSampleDAO().listSamplesWithPropertiesByExperiment(
                        experiment), false);
        assertFalse(sampleMap.isEmpty());

        int sampleCount = 0;
        for (SampleRowVO sample : query.getSamplesForExperiment(experimentId))
        {
            final String msg = "id: " + sample.id;
            final SampleRowVO sample2 = query.getSample(sample.id);
            assertTrue(msg, EqualsBuilder.reflectionEquals(sample, sample2));
            assertEquals(msg, experimentId, sample.expe_id.longValue());
            ++sampleCount;
        }
        assertEquals(sampleMap.size(), sampleCount);
    }

    @Test
    public void testQueryGroupWithExperimentSamples()
    {
        final Map<Long, SamplePE> sampleMap =
                createIdMap(daoFactory.getSampleDAO().listSamplesWithPropertiesByGroup(group), true);
        removeSamplesWithoutExperiments(sampleMap);
        assertFalse(sampleMap.isEmpty());

        int sampleCount = 0;
        for (SampleRowVO sample : query.getGroupSamplesWithExperiment(dbInstanceId, groupCode))
        {
            final String msg = "id: " + sample.id;
            final SampleRowVO sample2 = query.getSample(sample.id);
            assertTrue(msg, EqualsBuilder.reflectionEquals(sample, sample2));
            // We have to go the d-tour via samplePE as the sample doesn't contain the group id,
            // and no, we don't want to add it just for the test
            final SamplePE samplePE = sampleMap.get(sample.id);
            assertEquals(msg, groupId, samplePE.getGroup().getId().longValue());
            assertNotNull(msg, sample.expe_id);
            ++sampleCount;
        }
        assertEquals(sampleMap.size(), sampleCount);
    }

    private void removeSamplesWithoutExperiments(final Map<Long, SamplePE> sampleMap)
    {
        for (final Iterator<Map.Entry<Long, SamplePE>> it = sampleMap.entrySet().iterator(); it
                .hasNext(); /**/)
        {
            final Map.Entry<Long, SamplePE> entry = it.next();
            if (entry.getValue().getExperiment() == null)
            {
                it.remove();
            }
        }
    }

    @Test
    public void testQueryContainedSamples()
    {
        final Map<Long, SamplePE> sampleMap =
                createIdMap(daoFactory.getSampleDAO().listSamplesWithPropertiesByContainer(
                        firstMasterPlate), false);
        assertFalse(sampleMap.isEmpty());

        int sampleCount = 0;
        for (SampleRowVO sample : query.getSamplesForContainer(firstMasterPlate.getId()))
        {
            final String msg = "id: " + sample.id;
            final SampleRowVO sample2 = query.getSample(sample.id);
            assertTrue(msg, EqualsBuilder.reflectionEquals(sample, sample2));
            assertEquals(msg, firstMasterPlate.getId(), sample.samp_id_part_of);
            ++sampleCount;
        }
        assertEquals(sampleMap.size(), sampleCount);
    }

    @Test
    // TODO 2009-08-17, Bernd Rinn: This test is a stub!
    public void testQueryGroupSampleForSampleType()
    {
        int sampleCount = 0;
        for (@SuppressWarnings("unused")
        SampleRowVO sample : query.getGroupSamplesForSampleType(dbInstanceId, groupCode,
                masterPlateType.getId()))
        {
            // final String msg = "id: " + sample.id;
            ++sampleCount;
        }
        assertTrue(sampleCount > 0);
    }

    @Test
    // TODO 2009-08-17, Bernd Rinn: This test is a stub!
    public void testQuerySharedSamples()
    {
        int sampleCount = 0;
        for (@SuppressWarnings("unused")
        SampleRowVO sample : query.getSharedSamples(dbInstanceId))
        {
            // final String msg = "id: " + sample.id;
            ++sampleCount;
        }
        assertTrue(sampleCount > 0);
    }

    @Test
    // TODO 2009-08-17, Bernd Rinn: This test is a stub!
    public void testQuerySharedSamplesBySampleType()
    {
        int sampleCount = 0;
        for (@SuppressWarnings("unused")
        SampleRowVO sample : query.getSharedSamplesForSampleType(dbInstanceId, masterPlateType
                .getId()))
        {
            // final String msg = "id: " + sample.id;
            ++sampleCount;
        }
        assertTrue(sampleCount > 0);
    }

    @Test
    // TODO 2009-08-17, Bernd Rinn: This test is a stub!
    public void testGetExperimentAndProjectCode()
    {
        query.getExperimentAndProjectCodeForId(firstExperiment.getId());
    }

    @Test
    // TODO 2009-08-17, Bernd Rinn: This test is a stub!
    public void testGetExperimentAndProjectAndGroupCode()
    {
        query.getExperimentAndProjectAndGroupCodeForId(firstExperiment.getId());
    }

    @Test
    // TODO 2009-08-17, Bernd Rinn: This test is a stub!
    public void testPerson()
    {
        query.getPersonById(firstPerson.getId());
    }

    @Test
    // TODO 2009-08-17, Bernd Rinn: This test is a stub!
    public void testSampleType()
    {
        query.getSampleType(SAMPLE_TYPE_CODE_MASTER_PLATE);
    }

    @Test
    // TODO 2009-08-17, Bernd Rinn: This test is a stub!
    public void testSampleTypes()
    {
        query.getSampleTypes();
    }

    @Test
    // TODO 2009-08-17, Bernd Rinn: This test is a stub!
    public void testMaterialTypes()
    {
        query.getMaterialTypes();
    }

    @Test
    // TODO 2009-08-17, Bernd Rinn: This test is a stub!
    public void testPropertyTypes()
    {
        query.getPropertyTypes();
    }

    @Test
    // TODO 2009-08-17, Bernd Rinn: This test is a stub!
    public void testVocabuaryURLTemplates()
    {
        query.getVocabularyURLTemplates();
    }

    @Test
    // TODO 2009-08-17, Bernd Rinn: This test is a stub!
    public void testSamplePropertiesGenericValues()
    {
        query.getSamplePropertyGenericValues();
    }

    @Test
    // TODO 2009-08-17, Bernd Rinn: This test is a stub!
    public void testSamplePropertiesGenericValuesForSample()
    {
        query.getSamplePropertyGenericValues(firstMasterPlate.getId());
    }

    @Test
    // TODO 2009-08-17, Bernd Rinn: This test is a stub!
    public void testSamplePropertiesMaterialValues()
    {
        query.getSamplePropertyMaterialValues();
    }

    @Test
    // TODO 2009-08-17, Bernd Rinn: This test is a stub!
    public void testSamplePropertiesMaterialValuesForSample()
    {
        query.getSamplePropertyMaterialValues(firstMasterPlate.getId());
    }

    @Test
    // TODO 2009-08-17, Bernd Rinn: This test is a stub!
    public void testSamplePropertiesVocabularyTermValues()
    {
        query.getSamplePropertyVocabularyTermValues();
    }

    @Test
    // TODO 2009-08-17, Bernd Rinn: This test is a stub!
    public void testSamplePropertiesVocabularyTermValuesForSample()
    {
        query.getSamplePropertyVocabularyTermValues(firstMasterPlate.getId());
    }

}
