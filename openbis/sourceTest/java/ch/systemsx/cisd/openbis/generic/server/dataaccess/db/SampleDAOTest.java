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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.classic.Session;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.SampleOwner;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.HierarchyType;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
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

    private final SamplePE findSample(String code, String groupCode)
    {
        final ISampleDAO sampleDAO = daoFactory.getSampleDAO();
        final GroupPE group = findGroup("CISD");
        final SamplePE sample = sampleDAO.tryFindByCodeAndGroup(code, group, HierarchyType.CHILD);
        assertNotNull(sample);

        return sample;
    }

    private final void deleteSampleWithCollectionsChecked(SamplePE deletedSample,
            Set<Collection<?>> emptyCollections, Collection<?> nonEmptyCollection)
            throws DataAccessException
    {
        // check collections
        for (Collection<?> collection : emptyCollections)
        {
            assertTrue(collection.isEmpty());
        }
        if (nonEmptyCollection != null)
        {
            assertFalse(nonEmptyCollection.isEmpty());
        }

        // delete
        daoFactory.getSampleDAO().delete(deletedSample);
    }

    /**
     * @return Set of all collections that should prevent deletion of given <var>sample</var> if
     *         they are not empty.
     */
    private final Set<Collection<?>> getAllCollectionsThatPreventFromDeletion(SamplePE sample)
    {
        // deleted sample can not have: attachments, data sets, derived/contained samples
        Set<Collection<?>> result = new HashSet<Collection<?>>();
        result.add(sample.getAttachments());
        result.add(sample.getDatasets());
        result.add(sample.getGenerated());
        result.add(sample.getContained());
        return result;
    }

    @Test
    public final void testDeleteWithParentAndExperimentPreserved()
    {
        final ISampleDAO sampleDAO = daoFactory.getSampleDAO();
        final SamplePE deletedSample = findSample("3VCP5", "CISD");
        Set<Collection<?>> emptyCollections =
                getAllCollectionsThatPreventFromDeletion(deletedSample);
        deleteSampleWithCollectionsChecked(deletedSample, emptyCollections, null);

        // test successful deletion of sample
        assertNull(sampleDAO.tryGetByTechId(TechId.create(deletedSample)));

        // deleted sample had objects connected that should not have been deleted:
        // - a parent
        SamplePE generatedFrom = deletedSample.getGeneratedFrom();
        assertNotNull(generatedFrom);
        assertNotNull(sampleDAO.tryGetByTechId(new TechId(HibernateUtils.getId(generatedFrom))));
        // - an experiment
        ExperimentPE experiment = deletedSample.getExperiment();
        assertNotNull(experiment);
        assertNotNull(daoFactory.getExperimentDAO().tryGetByTechId(
                new TechId(HibernateUtils.getId(experiment))));
    }

    @Test
    public final void testDeleteWithProperties()
    {
        final ISampleDAO sampleDAO = daoFactory.getSampleDAO();
        final SamplePE deletedSample = findSample("EMPTY-MP", "CISD");
        Set<Collection<?>> emptyCollections =
                getAllCollectionsThatPreventFromDeletion(deletedSample);
        deleteSampleWithCollectionsChecked(deletedSample, emptyCollections, null);

        // test successful deletion of sample
        assertNull(sampleDAO.tryGetByTechId(TechId.create(deletedSample)));

        // test successful deletion of sample properties
        assertFalse(deletedSample.getProperties().isEmpty());
        List<EntityTypePropertyTypePE> retrievedPropertyTypes =
                daoFactory.getEntityPropertyTypeDAO(EntityKind.SAMPLE).listEntityPropertyTypes(
                        deletedSample.getEntityType());
        for (SamplePropertyPE property : deletedSample.getProperties())
        {
            int index = retrievedPropertyTypes.indexOf(property.getEntityTypePropertyType());
            EntityTypePropertyTypePE retrievedPropertyType = retrievedPropertyTypes.get(index);
            assertFalse(retrievedPropertyType.getPropertyValues().contains(property));
        }
    }

    @Test(expectedExceptions = DataIntegrityViolationException.class)
    public final void testDeleteFailWithAttachments()
    {
        final SamplePE deletedSample = findSample("3VCP6", "CISD");

        // deleted sample has should have attachments which should prevent it from deletion
        Collection<?> nonEmptyCollection = deletedSample.getAttachments();
        Set<Collection<?>> emptyCollections =
                getAllCollectionsThatPreventFromDeletion(deletedSample);
        emptyCollections.remove(nonEmptyCollection);

        deleteSampleWithCollectionsChecked(deletedSample, emptyCollections, nonEmptyCollection);
    }

    @Test(expectedExceptions = DataIntegrityViolationException.class)
    public final void testDeleteFailWithDatasets()
    {
        final SamplePE deletedSample = findSample("CP-TEST-1", "CISD");

        // deleted sample has should have data sets which should prevent it from deletion
        Collection<?> nonEmptyCollection = deletedSample.getDatasets();
        Set<Collection<?>> emptyCollections =
                getAllCollectionsThatPreventFromDeletion(deletedSample);
        emptyCollections.remove(nonEmptyCollection);

        deleteSampleWithCollectionsChecked(deletedSample, emptyCollections, nonEmptyCollection);
    }

    @Test(expectedExceptions = DataIntegrityViolationException.class)
    public final void testDeleteFailWithGeneratedSamples()
    {
        final SamplePE deletedSample = findSample("3VCP2", "CISD");

        // deleted sample has should have 'generated' samples which should prevent it from deletion
        Collection<?> nonEmptyCollection = deletedSample.getGenerated();
        Set<Collection<?>> emptyCollections =
                getAllCollectionsThatPreventFromDeletion(deletedSample);
        emptyCollections.remove(nonEmptyCollection);

        deleteSampleWithCollectionsChecked(deletedSample, emptyCollections, nonEmptyCollection);
    }

    @Test(expectedExceptions = DataIntegrityViolationException.class)
    public final void testDeleteFailWithContainedSamples()
    {
        final SamplePE deletedSample = findSample("C1", "CISD");

        // deleted sample has should have 'contained' samples which should prevent it from deletion
        Collection<?> nonEmptyCollection = deletedSample.getContained();
        Set<Collection<?>> emptyCollections =
                getAllCollectionsThatPreventFromDeletion(deletedSample);
        emptyCollections.remove(nonEmptyCollection);

        deleteSampleWithCollectionsChecked(deletedSample, emptyCollections, nonEmptyCollection);
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
