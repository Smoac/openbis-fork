/*
 * Copyright 2007 ETH Zuerich, CISD
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

import java.util.Arrays;
import java.util.List;

import org.hibernate.classic.Session;
import org.springframework.dao.DataIntegrityViolationException;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.SampleOwner;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.HierarchyType;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.types.SampleTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * Test cases for corresponding {@link SampleDAO} class.
 * 
 * @author Tomasz Pylak
 */
@Test(groups =
    { "db", "sample" })
@Friend(toClasses = SamplePE.class)
public final class SampleDAOTest extends AbstractDAOTest
{

    @Test
    public final void testListGroupSamples()
    {
        final SamplePE sample = createGroupSample();
        final List<SamplePE> samples =
                daoFactory.getSampleDAO().listSamplesWithPropertiesByTypeAndGroup(
                        sample.getSampleType(), sample.getGroup());
        assertEquals(1, samples.size());
        assertEquals(sample, samples.get(0));
    }

    @Test
    public final void testListSamplesFetchRelations()
    {
        final SampleTypePE type1 = getSampleType(SampleTypeCode.MASTER_PLATE);
        final SampleTypePE type2 = getSampleType(SampleTypeCode.DILUTION_PLATE);
        final SampleTypePE type3 = getSampleType(SampleTypeCode.CELL_PLATE);
        type3.setContainerHierarchyDepth(1);
        type3.setGeneratedFromHierarchyDepth(1);
        final SamplePE sampleA = createSample(type1, "grandParent", null);
        final SamplePE sampleB = createSample(type2, "parent", sampleA);
        SamplePE sampleC = createSample(type3, "child", sampleB);
        save(sampleA, sampleB, sampleC);
        final SamplePE well = createSample(type3, "well", null);
        final SamplePE container = createSample(type2, "container", null);
        final SamplePE superContainer = createSample(type2, "superContainer", null);
        well.setContainer(container);
        container.setContainer(superContainer);
        save(superContainer, container, well);

        // clear session to avoid using samples from first level cache
        final Session currentSession = sessionFactory.getCurrentSession();
        currentSession.flush();
        currentSession.clear();

        final List<SamplePE> samples = listSamplesFromHomeDatabase(type3);
        final SamplePE foundWell = findSample(well, samples);
        assertTrue(HibernateUtils.isInitialized(foundWell.getContainer()));
        final SamplePE foundContainer = foundWell.getContainer();
        assertFalse(HibernateUtils.isInitialized(foundContainer.getContainer()));
        sampleC = findSample(sampleC, samples);
        assertTrue(HibernateUtils.isInitialized(sampleC.getGeneratedFrom()));
        final SamplePE parent = sampleC.getGeneratedFrom();
        assertFalse(HibernateUtils.isInitialized(parent.getGeneratedFrom()));
    }

    @Test
    public final void testTryFindByCodeAndDatabaseInstance()
    {
        final SampleTypePE sampleType = getSampleType(SampleTypeCode.MASTER_PLATE);
        final List<SamplePE> samples = listSamplesFromHomeDatabase(sampleType);
        final DatabaseInstancePE homeDatabaseInstance = daoFactory.getHomeDatabaseInstance();
        final SamplePE sample = samples.get(0);
        final ISampleDAO sampleDAO = daoFactory.getSampleDAO();
        boolean fail = true;
        try
        {
            sampleDAO.tryFindByCodeAndDatabaseInstance(null, null, HierarchyType.CHILD);
        } catch (final AssertionError e)
        {
            fail = false;
        }
        assertFalse(fail);
        assertEquals(sample, sampleDAO.tryFindByCodeAndDatabaseInstance(sample.getCode(),
                homeDatabaseInstance, HierarchyType.CHILD));
        assertNull(sampleDAO.tryFindByCodeAndDatabaseInstance("", homeDatabaseInstance,
                HierarchyType.CHILD));
    }

    @Test
    public final void testTryFindByCodeAndGroup()
    {
        final ISampleDAO sampleDAO = daoFactory.getSampleDAO();
        final SamplePE sample = createGroupSample();
        boolean fail = true;
        try
        {
            sampleDAO.tryFindByCodeAndGroup(null, null, HierarchyType.CHILD);
        } catch (final AssertionError e)
        {
            fail = false;
        }
        assertFalse(fail);
        assertEquals(sample, sampleDAO.tryFindByCodeAndGroup(sample.getCode(), sample.getGroup(),
                HierarchyType.CHILD));
        assertNull(sampleDAO.tryFindByCodeAndGroup("", sample.getGroup(), HierarchyType.CHILD));
    }

    @Test
    public final void testListSamplesByContainer()
    {
        final ISampleDAO sampleDAO = daoFactory.getSampleDAO();
        boolean fail = true;
        try
        {
            sampleDAO.listSamplesWithPropertiesByContainer(null);
        } catch (final AssertionError e)
        {
            fail = false;
        }
        assertFalse(fail);
        final String masterPlateCode = "MP";
        final DatabaseInstancePE homeInstance = daoFactory.getHomeDatabaseInstance();
        final SamplePE sample =
                sampleDAO.tryFindByCodeAndDatabaseInstance(masterPlateCode, homeInstance,
                        HierarchyType.CHILD);
        assertNotNull(sample);
        final List<SamplePE> samples = sampleDAO.listSamplesWithPropertiesByContainer(sample);
        assertEquals(320, samples.size());
    }

    @Test(expectedExceptions = DataIntegrityViolationException.class)
    public final void testFailDeleteWithPropertiesDerivedAndContainedSamples()
    {
        // deleted sample has properties, derived samples and contained samples connected
        // that should be deleted too

        final ISampleDAO sampleDAO = daoFactory.getSampleDAO();
        final String code = "MP1-MIXED";
        final GroupPE group = findGroup("CISD");
        final SamplePE deletedSample =
                sampleDAO.tryFindByCodeAndGroup(code, group, HierarchyType.CHILD);
        assertNotNull(deletedSample);

        sampleDAO.delete(deletedSample);

        // // test successful deletion of sample
        // assertEquals(null, sampleDAO.tryGetByTechId(TechId.create(deletedSample)));
        //
        // // test successful deletion of connected:
        // // - derived samples
        // assertEqualsOrGreater(1, deletedSample.getDerived().size());
        // for (SamplePE sample : deletedSample.getDerived())
        // {
        // assertEquals(null, daoFactory.getSampleDAO().tryGetByTechId(TechId.create(sample)));
        // }
        // // - contained samples
        // assertEqualsOrGreater(1, deletedSample.getContained().size());
        // for (SamplePE sample : deletedSample.getContained())
        // {
        // assertEquals(null, daoFactory.getSampleDAO().tryGetByTechId(TechId.create(sample)));
        // }
        // // - properties
        // assertEqualsOrGreater(1, deletedSample.getProperties().size());
        // List<EntityTypePropertyTypePE> retrievedPropertyTypes =
        // daoFactory.getEntityPropertyTypeDAO(EntityKind.SAMPLE).listEntityPropertyTypes(
        // deletedSample.getEntityType());
        // for (SamplePropertyPE property : deletedSample.getProperties())
        // {
        // int index = retrievedPropertyTypes.indexOf(property.getEntityTypePropertyType());
        // EntityTypePropertyTypePE retrievedPropertyType = retrievedPropertyTypes.get(index);
        // assertFalse(retrievedPropertyType.getPropertyValues().contains(property));
        // }
    }

    // TODO 2009-06-09, Piotr Buczek: write additional tests that check all foreign key violations
    @Test(expectedExceptions = DataIntegrityViolationException.class)
    public final void testFailDeleteWithDatasets()
    {
        // deleted sample has properties and data sets connected that should be deleted too

        final ISampleDAO sampleDAO = daoFactory.getSampleDAO();
        final String code = "3VCP1";
        final GroupPE group = findGroup("CISD");
        final SamplePE deletedSample =
                sampleDAO.tryFindByCodeAndGroup(code, group, HierarchyType.CHILD);
        assertNotNull(deletedSample);

        sampleDAO.delete(deletedSample);

        // // test successful deletion of sample
        // assertNull(sampleDAO.tryGetByTechId(TechId.create(deletedSample)));
        //
        // // test successful deletion of connected:
        // // - data sets
        // assertEqualsOrGreater(1, deletedSample.getDatasets().size());
        // for (DataPE data : deletedSample.getDatasets())
        // {
        // assertNull(daoFactory.getExternalDataDAO().tryGetByTechId(TechId.create(data)));
        // }
        //
        // // test that parent sample is preserved
        // SamplePE generatedFrom = deletedSample.getGeneratedFrom();
        // assertNotNull(generatedFrom);
        // assertNotNull(daoFactory.getSampleDAO().tryGetByTechId(
        // new TechId(HibernateUtils.getId(generatedFrom))));
    }

    @Test
    public final void testListSamplesBySimpleProperty()
    {
        final ISampleDAO sampleDAO = daoFactory.getSampleDAO();
        GroupPE group = findGroup("CISD");

        List<SamplePE> samples = sampleDAO.listSamplesByGroupAndProperty("USER.SIZE", "321", group);

        assertEquals(1, samples.size());
        assertEquals("CP-TEST-2", samples.get(0).getCode());
    }

    @Test
    public final void testListSamplesByVocabularyTermProperty()
    {
        final ISampleDAO sampleDAO = daoFactory.getSampleDAO();
        GroupPE group = findGroup("CISD");

        List<SamplePE> samples =
                sampleDAO.listSamplesByGroupAndProperty("USER.ORGANISM", "HUMAN", group);
        assertEquals(1, samples.size());
        assertEquals("CP-TEST-1", samples.get(0).getCode());
    }

    private GroupPE findGroup(String groupCode)
    {
        GroupPE group =
                daoFactory.getGroupDAO().tryFindGroupByCodeAndDatabaseInstance(groupCode,
                        daoFactory.getHomeDatabaseInstance());
        assert group != null : "group not found: " + groupCode;
        return group;
    }

    @Test
    public final void testTryFindCodeAndDatabaseInstanceWithUniqueResult()
    {
        final ISampleDAO sampleDAO = daoFactory.getSampleDAO();
        final SamplePE samplePE = new SamplePE();
        final String sampleCode = "A03";
        samplePE.setCode(sampleCode);
        samplePE.setPermId(daoFactory.getPermIdDAO().createPermId());
        samplePE.setSampleType(daoFactory.getSampleTypeDAO().tryFindSampleTypeByCode(
                SampleTypeCode.DILUTION_PLATE.getCode()));
        final DatabaseInstancePE homeDatabaseInstance = daoFactory.getHomeDatabaseInstance();
        samplePE.setDatabaseInstance(homeDatabaseInstance);
        samplePE.setRegistrator(getSystemPerson());
        sampleDAO.createSample(samplePE);
        // Following line throws a NonUniqueResultException if sample code not unique.
        sampleDAO.tryFindByCodeAndDatabaseInstance(sampleCode, homeDatabaseInstance,
                HierarchyType.CHILD);
    }

    //
    // Private methods
    //

    private final List<SamplePE> listSamplesFromHomeDatabase(final SampleTypePE sampleType)
    {
        final ISampleDAO sampleDAO = daoFactory.getSampleDAO();
        return sampleDAO.listSamplesWithPropertiesByTypeAndDatabaseInstance(sampleType, daoFactory
                .getHomeDatabaseInstance());
    }

    private final SamplePE findSample(final SamplePE sample, final List<SamplePE> samples)
    {
        final int sampleIx = samples.indexOf(sample);
        assert sampleIx != -1 : "sample not found " + sample;
        return samples.get(sampleIx);
    }

    private final void save(final SamplePE... samples)
    {
        final ISampleDAO sampleDAO = daoFactory.getSampleDAO();
        if (samples.length > 1)
        {
            sampleDAO.createSamples(Arrays.asList(samples));
        } else
        {
            sampleDAO.createSample(samples[0]);
        }
    }

    private final SampleTypePE getSampleType(final SampleTypeCode sampleTypeCode)
    {
        final SampleTypePE sampleType =
                daoFactory.getSampleTypeDAO().tryFindSampleTypeByCode(sampleTypeCode.getCode());
        assert sampleType != null;
        return sampleType;
    }

    /**
     * Creates a group sample in the database.
     */
    private final SamplePE createGroupSample()
    {
        final SampleTypePE sampleType = getSampleType(SampleTypeCode.MASTER_PLATE);
        final GroupPE group = createGroup("xxx");
        daoFactory.getGroupDAO().createGroup(group);
        final SamplePE sample =
                createSample(sampleType, "code", null, SampleOwner.createGroup(group));
        save(sample);
        assertNotNull(sample);
        assertNotNull(sample.getSampleType());
        assertNotNull(sample.getGroup());
        return sample;
    }

    private final SamplePE createSample(final SampleTypePE type, final String code,
            final SamplePE generatorOrNull)
    {
        final SampleOwner owner =
                SampleOwner.createDatabaseInstance(daoFactory.getHomeDatabaseInstance());
        return createSample(type, code, generatorOrNull, owner);
    }

    private final SamplePE createSample(final SampleTypePE type, final String code,
            final SamplePE generatorOrNull, final SampleOwner sampleOwner)
    {
        final SamplePE sample = new SamplePE();
        sample.setRegistrator(getSystemPerson());
        sample.setCode(code);
        sample.setPermId(daoFactory.getPermIdDAO().createPermId());
        sample.setSampleType(type);
        sample.setDatabaseInstance(sampleOwner.tryGetDatabaseInstance());
        sample.setGroup(sampleOwner.tryGetGroup());
        sample.setGeneratedFrom(generatorOrNull);
        return sample;
    }
}
