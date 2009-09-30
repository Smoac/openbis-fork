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

import static ch.systemsx.cisd.openbis.generic.server.business.bo.common.EntityListingTestUtils.asList;
import static ch.systemsx.cisd.openbis.generic.server.business.bo.common.EntityListingTestUtils.findCode;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.CodeRecord;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.EntityListingTestUtils;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.entity.ExperimentProjectGroupCodeRecord;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.AbstractDAOTest;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * Test cases for {@link ISampleListingQuery}.
 * 
 * @author Bernd Rinn
 */
@Friend(toClasses =
    { SampleRecord.class, ExperimentProjectGroupCodeRecord.class, ISampleListingQuery.class,
            SampleListerDAO.class })
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

    private ISampleListingQuery query;

    @BeforeClass(alwaysRun = true)
    public void init() throws SQLException
    {
        SampleListerDAO sampleListerDAO = createSampleListerDAO(daoFactory);
        dbInstanceId = sampleListerDAO.getDatabaseInstanceId();
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
        query = sampleListerDAO.getQuery();
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws SQLException
    {
        if (query != null)
        {
            query.commit();
        }
    }

    public static SampleListerDAO createSampleListerDAO(IDAOFactory daoFactory)
    {
        ISampleListingQuery query =
                EntityListingTestUtils.createQuery(daoFactory, ISampleListingQuery.class);
        return SampleListerDAO.create(daoFactory, query);
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
        final long listableSamplesInTestDB = query.getSampleCount(dbInstanceId);
        assertTrue(listableSamplesInTestDB > 0);
        int sampleCount = 0;
        for (SampleRecord sample : query.getSamples(dbInstanceId))
        {
            final String msg = "id: " + sample.id;
            final SampleRecord sample2 = query.getSample(sample.id);
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
        for (SampleRecord sample : query.getGroupSamples(dbInstanceId, groupCode))
        {
            // Note: query.getGroupSamples() doesn't query for grou_id as it is not used by the
            // business code
            sample.grou_id = groupId;
            final String msg = "id: " + sample.id;
            final SampleRecord sample2 = query.getSample(sample.id);
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
        List<SampleRecord> samples =
                asList(query.getGroupSamplesForSampleType(dbInstanceId, groupCode, sampleTypeId));
        assertTrue(samples.size() >= 15);
        SampleRecord sample = findCode(samples, "CP-TEST-1");
        assertEquals(18, sample.expe_id.longValue());
        assertEquals(1042, sample.id);
        assertEquals(2, sample.pers_id_registerer);
        assertEquals(sampleTypeId, sample.saty_id);
        assertNotNull(sample.perm_id);
        assertNull(sample.samp_id_generated_from);
        assertNull(sample.samp_id_part_of);

        SampleRecord sample2 = findCode(samples, "3VCP1");
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
        for (SampleRecord sample : query.getSamplesForExperiment(experimentId))
        {
            // Note: getSamplesForExperiment() doesn't query for grou_id as it is not used by the
            // business code
            sample.grou_id = groupId;
            final String msg = "id: " + sample.id;
            final SampleRecord sample2 = query.getSample(sample.id);
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
        for (SampleRecord sample : query.getGroupSamplesWithExperiment(dbInstanceId, groupCode))
        {
            // Note: getGroupSamplesWithExperiment() doesn't query for grou_id as it is not used by
            // the business code
            sample.grou_id = groupId;
            final String msg = "id: " + sample.id;
            final SampleRecord sample2 = query.getSample(sample.id);
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
        for (SampleRecord sample : query.getSamplesForContainer(firstMasterPlate.getId()))
        {
            final String msg = "id: " + sample.id;
            final SampleRecord sample2 = query.getSample(sample.id);
            assertTrue(msg, EqualsBuilder.reflectionEquals(sample, sample2));
            assertEquals(msg, firstMasterPlate.getId(), sample.samp_id_part_of);
            ++sampleCount;
        }
        assertEquals(sampleMap.size(), sampleCount);
    }

    @Test
    public void testQuerySharedSamples()
    {
        List<SampleRecord> samples = asList(query.getSharedSamples(dbInstanceId));
        assertTrue(samples.size() > 0);
        SampleRecord sample = findCode(samples, SHARED_MASTER_PLATE_CODE);
        assertEquals(SHARED_MASTER_PLATE_ID, sample.id);
    }

    @Test
    public void testQuerySharedSamplesBySampleType()
    {
        List<SampleRecord> samples =
                asList(query.getSharedSamplesForSampleType(dbInstanceId, masterPlateType.getId()));
        assertTrue(samples.size() > 0);
        SampleRecord sample = findCode(samples, SHARED_MASTER_PLATE_CODE);
        assertEquals(SHARED_MASTER_PLATE_ID, sample.id);
    }

    @Test
    public void testSampleType()
    {
        SampleType sampleType = query.getSampleType(dbInstanceId, SAMPLE_TYPE_CODE_MASTER_PLATE);
        assertEquals(SAMPLE_TYPE_CODE_MASTER_PLATE, sampleType.getCode());
    }

    @Test
    public void testSampleTypes()
    {
        List<SampleType> sampleTypes = Arrays.asList(query.getSampleTypes(dbInstanceId));
        findCode(sampleTypes, SAMPLE_TYPE_CODE_MASTER_PLATE);
        findCode(sampleTypes, SAMPLE_TYPE_CODE_CELL_PLATE);
    }

    @Test
    public void testMaterialTypes()
    {
        CodeRecord[] materialTypes = query.getMaterialTypes();
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
        query.getVocabularyURLTemplates();
    }
}
