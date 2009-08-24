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
import static ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.SampleListingTestUtils.findCode;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.lemnik.eodsql.DataIterator;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleListingQuery;
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleListingQuery.BaseSamplePropertyVO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleListingQuery.CoVoSamplePropertyVO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleListingQuery.CodeVO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleListingQuery.ExperimentProjectGroupCodeVO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleListingQuery.GenericSamplePropertyVO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleListingQuery.MaterialSamplePropertyVO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleListingQuery.SampleRowVO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.AbstractDAOTest;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
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

    private static final String MATERIAL_TYPE_CODE = "GENE";

    private static final String SHARED_MASTER_PLATE_CODE = "MP";

    private static final int SHARED_MASTER_PLATE_ID = 646;

    private static final String SAMPLE_TYPE_CODE_MASTER_PLATE = "MASTER_PLATE";

    private static final String SAMPLE_TYPE_CODE_CELL_PLATE = "CELL_PLATE";

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
        group = daoFactory.getGroupDAO().tryFindGroupByCodeAndDatabaseInstance("CISD", dbInstance);
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
    public void testQueryGroupSamplesBySampleType()
    {
        long sampleTypeId = getSampleTypeId(SAMPLE_TYPE_CODE_CELL_PLATE);
        List<SampleRowVO> samples =
                asList(query.getGroupSamplesForSampleType(dbInstanceId, groupCode, sampleTypeId));
        assertTrue(samples.size() >= 15);
        SampleRowVO sample = findCode(samples, "CP-TEST-1");
        assertEquals(18, sample.expe_id.longValue());
        assertEquals(1042, sample.id);
        assertEquals(2, sample.pers_id_registerer);
        assertEquals(sampleTypeId, sample.saty_id);
        assertNotNull(sample.perm_id);
        assertNull(sample.samp_id_generated_from);
        assertNull(sample.samp_id_part_of);

        SampleRowVO sample2 = findCode(samples, "3VCP1");
        assertNotNull(sample2.samp_id_generated_from);
        assertNull(sample2.samp_id_part_of);
    }


    private Long getSampleTypeId(String sampleTypeCode)
    {
        return daoFactory.getSampleTypeDAO().tryFindSampleTypeByCode(sampleTypeCode).getId();
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
    public void testQuerySharedSamples()
    {
        List<SampleRowVO> samples = asList(query.getSharedSamples(dbInstanceId));
        assertTrue(samples.size() > 0);
        SampleRowVO sample = findCode(samples, SHARED_MASTER_PLATE_CODE);
        assertEquals(SHARED_MASTER_PLATE_ID, sample.id);
    }

    @Test
    public void testQuerySharedSamplesBySampleType()
    {
        List<SampleRowVO> samples =
                asList(query.getSharedSamplesForSampleType(dbInstanceId, masterPlateType.getId()));
        assertTrue(samples.size() > 0);
        SampleRowVO sample = findCode(samples, SHARED_MASTER_PLATE_CODE);
        assertEquals(SHARED_MASTER_PLATE_ID, sample.id);
    }

    @Test
    public void testGetExperiment()
    {
        ExperimentProjectGroupCodeVO expFull =
                query.getExperimentAndProjectAndGroupCodeForId(firstExperiment.getId());
        assertEquals(firstExperiment.getCode(), expFull.e_code);
        ProjectPE project = firstExperiment.getProject();
        assertEquals(project.getCode(), expFull.p_code);
        assertEquals(project.getGroup().getCode(), expFull.g_code);
        assertEquals(firstExperiment.getEntityType().getCode(), expFull.et_code);

        ExperimentProjectGroupCodeVO expNoGroup =
                query.getExperimentAndProjectCodeForId(firstExperiment.getId());
        assertNull(expNoGroup.g_code);
        expFull.g_code = null;
        assertTrue(EqualsBuilder.reflectionEquals(expNoGroup, expFull));
    }

    @Test
    public void testPerson()
    {
        Person person = query.getPersonById(firstPerson.getId());
        assertEquals(firstPerson.getFirstName(), person.getFirstName());
    }

    @Test
    public void testSampleType()
    {
        SampleType sampleType = query.getSampleType(SAMPLE_TYPE_CODE_MASTER_PLATE);
        assertEquals(SAMPLE_TYPE_CODE_MASTER_PLATE, sampleType.getCode());
    }

    @Test
    public void testSampleTypes()
    {
        List<SampleType> sampleTypes = Arrays.asList(query.getSampleTypes());
        findCode(sampleTypes, SAMPLE_TYPE_CODE_MASTER_PLATE);
        findCode(sampleTypes, SAMPLE_TYPE_CODE_CELL_PLATE);
    }

    @Test
    public void testMaterialTypes()
    {
        CodeVO[] materialTypes = query.getMaterialTypes();
        findCode(Arrays.asList(materialTypes), MATERIAL_TYPE_CODE);
    }

    @Test
    public void testPropertyTypes()
    {
        PropertyType[] propertyTypes = query.getPropertyTypes();
        PropertyType propertyType = findCode(Arrays.asList(propertyTypes), "COMMENT");
        assertEquals(propertyType.getDataType().getCode(), DataTypeCode.VARCHAR);
        assertEquals(propertyType.getLabel(), "Comment");
    }

    @Test
    public void testVocabuaryURLTemplates()
    {
        CodeVO[] vocabularyURLTemplates = query.getVocabularyURLTemplates();
        findCode(Arrays.asList(vocabularyURLTemplates), "GENDER");
    }

    @Test
    public void testSamplePropertiesGenericValues()
    {
        List<GenericSamplePropertyVO> properties = asList(query.getSamplePropertyGenericValues());
        assertCorrectSampleAndPropertyTypeReferences(properties);
        for (GenericSamplePropertyVO property : properties)
        {
            assertNotNull(property.value);
        }
        checkSamplePropertiesGenericValuesForSample(properties.iterator().next().samp_id);
    }

    private void assertCorrectSampleAndPropertyTypeReferences(
            List<? extends BaseSamplePropertyVO> properties)
    {
        assertTrue(properties.size() > 0);
        Set<Long> propertyTypesIds = extractIds(Arrays.asList(query.getPropertyTypes()));
        Set<Long> sampleIds = extractVOIds(asList(query.getSamples()));
        for (BaseSamplePropertyVO property : properties)
        {
            assertTrue("Property type not found " + property.prty_id, propertyTypesIds
                    .contains(property.prty_id));
            assertTrue("Sample not found " + property.samp_id, sampleIds.contains(property.samp_id));
        }
    }

    private static <T extends CodeVO> Set<Long> extractVOIds(List<T> items)
    {
        Set<Long> ids = new HashSet<Long>();
        for (T item : items)
        {
            ids.add(item.id);
        }
        return ids;
    }

    private static <T extends IIdHolder> Set<Long> extractIds(List<T> items)
    {
        Set<Long> ids = new HashSet<Long>();
        for (T item : items)
        {
            ids.add(item.getId());
        }
        return ids;
    }

    // entityId - id of a sample which has a property
    private void checkSamplePropertiesGenericValuesForSample(long entityId)
    {
        DataIterator<GenericSamplePropertyVO> properties =
                query.getSamplePropertyGenericValues(entityId);
        assertTrue("no generic properties found", properties.hasNext());
        for (GenericSamplePropertyVO property : properties)
        {
            assertNotNull(property.value);
            assertEquals(entityId, property.samp_id);
        }
    }

    @Test
    public void testSamplePropertiesMaterialValues()
    {
        List<MaterialSamplePropertyVO> properties = asList(query.getSamplePropertyMaterialValues());
        assertCorrectSampleAndPropertyTypeReferences(properties);
        for (MaterialSamplePropertyVO property : properties)
        {
            assertNotNull(property.code);
        }
        checkSamplePropertiesMaterialValuesForSample(properties.iterator().next().samp_id);
    }

    private void checkSamplePropertiesMaterialValuesForSample(long entityId)
    {
        DataIterator<MaterialSamplePropertyVO> properties =
                query.getSamplePropertyMaterialValues(entityId);
        assertTrue("no material properties found", properties.hasNext());
        for (MaterialSamplePropertyVO property : properties)
        {
            assertNotNull(property.code);
            assertEquals(entityId, property.samp_id);
        }
    }

    @Test
    public void testSamplePropertiesVocabularyTermValues()
    {
        List<CoVoSamplePropertyVO> properties =
                asList(query.getSamplePropertyVocabularyTermValues());
        assertCorrectSampleAndPropertyTypeReferences(properties);
        for (CoVoSamplePropertyVO property : properties)
        {
            assertNotNull(property.code);
        }
        checkSamplePropertiesVocabularyTermValuesForSample(properties.iterator().next().samp_id);
    }

    private void checkSamplePropertiesVocabularyTermValuesForSample(long entityId)
    {
        DataIterator<CoVoSamplePropertyVO> properties =
                query.getSamplePropertyVocabularyTermValues(entityId);
        assertTrue("no vocabulary properties found", properties.hasNext());
        for (CoVoSamplePropertyVO property : properties)
        {
            assertNotNull(property.code);
            assertEquals(entityId, property.samp_id);
        }

    }

}
