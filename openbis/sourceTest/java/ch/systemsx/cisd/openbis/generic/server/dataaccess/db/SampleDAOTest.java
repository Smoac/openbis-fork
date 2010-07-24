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
import static org.testng.AssertJUnit.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.hibernate.classic.Session;
import org.springframework.dao.DataIntegrityViolationException;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.SampleOwner;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEventDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventType;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleRelationshipPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

// FIXME 2010-07-24, Piotr Buczek: fix broken tests
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
    private static final String DILUTION_PLATE = "DILUTION_PLATE";

    private static final String MASTER_PLATE = "MASTER_PLATE";

    public final void testCodeUniqueness()
    {

    }

    @Test
    public final void testListSamplesFetchRelations()
    {
        final SampleTypePE type1 = getSampleType("MASTER_PLATE");
        final SampleTypePE type2 = getSampleType("DILUTION_PLATE");
        final SampleTypePE type3 = getSampleType("CELL_PLATE");
        type3.setContainerHierarchyDepth(1);
        type3.setGeneratedFromHierarchyDepth(1);
        SamplePE grandParent = createSample(type1, "grandParent");
        save(grandParent);
        SamplePE parent = createDerivedSample(type2, "parent", grandParent);
        save(parent);
        SamplePE child1 = createDerivedSample(type3, "child1", parent);
        save(child1);
        SamplePE child2 = createDerivedSample(type3, "child2", parent);
        save(child2);
        final SamplePE container = createSample(type2, "container");
        save(container);
        parent.setContainer(container);

        // clear session to avoid using samples from first level cache
        final Session currentSession = sessionFactory.getCurrentSession();
        currentSession.flush();
        currentSession.clear();

        final List<SamplePE> children =
                daoFactory.getSampleDAO().listSamplesByGeneratedFrom(parent);
        child1 = findSample(child1, children);
        child2 = findSample(child2, children);
        assertTrue(HibernateUtils.isInitialized(child1.getGeneratedFrom()));
        assertTrue(HibernateUtils.isInitialized(child2.getGeneratedFrom()));
        assertEquals(child1.getGeneratedFrom(), child2.getGeneratedFrom());

        assertEquals(parent, child1.getGeneratedFrom());
        parent = child1.getGeneratedFrom();
        assertTrue(HibernateUtils.isInitialized(parent.getGeneratedFrom()));
        assertEquals(grandParent, parent.getGeneratedFrom());
        assertTrue(HibernateUtils.isInitialized(parent.getContainer()));
        assertEquals(container, parent.getContainer());
    }

    private final SamplePE findSample(final SamplePE sample, final List<SamplePE> samples)
    {
        final int sampleIx = samples.indexOf(sample);
        assert sampleIx != -1 : "sample not found " + sample;
        return samples.get(sampleIx);
    }

    @Test
    public final void testDefaultSampleCodeUniqueness()
    {
        final SampleTypePE containerType = getSampleType("DILUTION_PLATE");
        final SampleTypePE containedType = getSampleType("CELL_PLATE");
        containedType.setContainerHierarchyDepth(1);
        containedType.setGeneratedFromHierarchyDepth(1);
        containedType.setSubcodeUnique(false);
        final SamplePE container1 = createSample(containerType, "container1");
        final SamplePE container2 = createSample(containerType, "container2");
        final SamplePE well1_1 = createContainedSample(containedType, "well1", container1);
        final SamplePE well1_2 = createContainedSample(containedType, "well2", container1);
        final SamplePE well2_1 = createContainedSample(containedType, "well1", container2);
        final SamplePE well2_2 = createContainedSample(containedType, "well2", container2);
        save(container1, container2, well1_1, well1_2, well2_1, well2_2);

        final Session currentSession = sessionFactory.getCurrentSession();
        currentSession.flush();

        try
        {
            final SamplePE duplicatedWell1_1 =
                    createContainedSample(containedType, "well1", container1);
            save(duplicatedWell1_1);
            currentSession.flush();
            fail("DataIntegrityViolationException expected");
        } catch (DataIntegrityViolationException e)
        {
            assertEquals("ERROR: Insert/Update of Sample (Code: WELL1) failed because "
                    + "database instance sample with the same code and being the part "
                    + "of the same container already exists.", e.getMessage());
        }
    }

    @Test
    public final void testSampleSubcodeUniqueness()
    {
        final SampleTypePE containerType = getSampleType("DILUTION_PLATE");
        final SampleTypePE containedType = getSampleType("CELL_PLATE");
        containedType.setContainerHierarchyDepth(1);
        containedType.setGeneratedFromHierarchyDepth(1);
        containedType.setSubcodeUnique(true);
        final SamplePE container1 = createSample(containerType, "container1");
        final SamplePE container2 = createSample(containerType, "container2");
        final SamplePE well1_1 = createContainedSample(containedType, "well1", container1);
        save(container1, container2, well1_1);

        final Session currentSession = sessionFactory.getCurrentSession();
        currentSession.flush();

        // subcode uniqueness should be checked
        try
        {
            final SamplePE well2_1 = createContainedSample(containedType, "well1", container2);
            save(well2_1);
            currentSession.flush();
            fail("DataIntegrityViolationException expected");
        } catch (DataIntegrityViolationException e)
        {
            assertEquals("ERROR: Insert/Update of Sample (Code: WELL1) failed because "
                    + "database instance sample of the same type with the same subcode "
                    + "already exists.", e.getMessage());
        }
    }

    @Test
    public final void testSampleSubcodeUniquenessAcrossTypes()
    {
        final SampleTypePE type1 = getSampleType("DILUTION_PLATE");
        final SampleTypePE type2 = getSampleType("CELL_PLATE");
        type2.setSubcodeUnique(true);
        final SamplePE sampleT1 = createSample(type1, "S_CODE");
        save(sampleT1);

        final Session currentSession = sessionFactory.getCurrentSession();
        currentSession.flush();

        // default uniqueness should still be checked across types
        try
        {
            final SamplePE sampleT2 = createSample(type2, "S_CODE");
            save(sampleT2);
            currentSession.flush();
            fail("DataIntegrityViolationException expected");
        } catch (DataIntegrityViolationException e)
        {
            assertEquals("ERROR: Insert/Update of Sample (Code: S_CODE) failed because "
                    + "database instance sample with the same code already exists.", e.getMessage());
        }
    }

    @Test
    public void testTryToFindByPermID()
    {
        String permID = "200811050919915-8";
        SamplePE sample = daoFactory.getSampleDAO().tryToFindByPermID(permID);

        assertEquals(permID, sample.getPermId());
        Set<SamplePropertyPE> properties = sample.getProperties();
        assertEquals(2, properties.size());
        Iterator<SamplePropertyPE> iterator = properties.iterator();
        SamplePropertyPE description = iterator.next();
        SamplePropertyPE plateGeometry = iterator.next();
        if (plateGeometry.getVocabularyTerm() == null)
        {
            SamplePropertyPE p = plateGeometry;
            plateGeometry = description;
            description = p;
        }
        assertEquals("DESCRIPTION", description.getEntityTypePropertyType().getPropertyType()
                .getCode());
        assertEquals("test control layout", description.getValue());
        assertEquals("$PLATE_GEOMETRY", plateGeometry.getEntityTypePropertyType().getPropertyType()
                .getCode());
        assertEquals("384_WELLS_16X24", plateGeometry.getVocabularyTerm().getCode());
    }

    @Test
    public final void testTryFindByCodeAndDatabaseInstance()
    {
        final DatabaseInstancePE homeDatabaseInstance = daoFactory.getHomeDatabaseInstance();
        final ISampleDAO sampleDAO = daoFactory.getSampleDAO();
        boolean fail = true;
        try
        {
            sampleDAO.tryFindByCodeAndDatabaseInstance(null, null);
        } catch (final AssertionError e)
        {
            fail = false;
        }
        assertFalse(fail);
        assertNotNull(sampleDAO.tryFindByCodeAndDatabaseInstance("MP", homeDatabaseInstance));
        assertNull(sampleDAO.tryFindByCodeAndDatabaseInstance("", homeDatabaseInstance));
    }

    @Test
    public final void testTryFindByCodeAndGroup()
    {
        final ISampleDAO sampleDAO = daoFactory.getSampleDAO();
        final SamplePE sample = createGroupSample();
        boolean fail = true;
        try
        {
            sampleDAO.tryFindByCodeAndGroup(null, null);
        } catch (final AssertionError e)
        {
            fail = false;
        }
        assertFalse(fail);
        assertEquals(sample, sampleDAO.tryFindByCodeAndGroup(sample.getCode(), sample.getGroup()));
        assertNull(sampleDAO.tryFindByCodeAndGroup("", sample.getGroup()));
    }

    private final SamplePE findSample(String code, String groupCode)
    {
        final ISampleDAO sampleDAO = daoFactory.getSampleDAO();
        final GroupPE group = findGroup("CISD");
        final SamplePE sample = sampleDAO.tryFindByCodeAndGroup(code, group);
        assertNotNull(sample);

        return sample;
    }

    @Test(groups = "broken")
    public final void testDeleteWithParentAndExperimentPreserved()
    {
        final ISampleDAO sampleDAO = daoFactory.getSampleDAO();
        final SamplePE deletedSample = findSample("3VCP5", "CISD");

        // Deleted sample should have all collections which prevent it from deletion empty.
        assertTrue(deletedSample.getAttachments().isEmpty());
        assertTrue(deletedSample.getDatasets().isEmpty());
        assertTrue(deletedSample.getGenerated().isEmpty());
        assertTrue(deletedSample.getContained().isEmpty());

        SamplePE generatedFrom = deletedSample.getGeneratedFrom();
        assertNotNull(generatedFrom);
        ExperimentPE experiment = deletedSample.getExperiment();
        assertNotNull(experiment);

        // delete
        deleteSample(deletedSample);

        // test successful deletion of sample
        assertNull(sampleDAO.tryGetByTechId(TechId.create(deletedSample)));

        // deleted sample had objects connected that should not be deleted:
        // - a parent
        assertNotNull(sampleDAO.tryGetByTechId(new TechId(HibernateUtils.getId(generatedFrom))));
        // - an experiment
        assertNotNull(daoFactory.getExperimentDAO().tryGetByTechId(
                new TechId(HibernateUtils.getId(experiment))));
    }

    private void deleteSample(SamplePE sample)
    {
        // before deletion there shouldn't be any entry about deletion of the sample in event table
        assertNull(tryGetDeletionEvent(sample));

        List<TechId> sampleIds = new ArrayList<TechId>();
        sampleIds.add(TechId.create(sample));
        final PersonPE registrator = getSystemPerson();
        final String reason = "reason" + sample.getPermId();

        daoFactory.getSampleDAO().delete(sampleIds, registrator, reason);

        // after deletion there should be an entry about deletion of the sample in event table
        final EventPE event = tryGetDeletionEvent(sample);
        assertNotNull(event);
        assertEquals(reason, event.getReason());
        assertEquals(registrator, event.getRegistrator());
    }

    private EventPE tryGetDeletionEvent(SamplePE sample)
    {
        final IEventDAO eventDAO = daoFactory.getEventDAO();
        return eventDAO.tryFind(sample.getPermId(), EntityType.SAMPLE, EventType.DELETION);
    }

    @Test(groups = "broken")
    public final void testDeleteWithProperties()
    {
        final ISampleDAO sampleDAO = daoFactory.getSampleDAO();
        final SamplePE deletedSample = findSample("EMPTY-MP", "CISD");

        // Deleted sample should have all collections which prevent it from deletion empty.
        assertTrue(deletedSample.getAttachments().isEmpty());
        assertTrue(deletedSample.getDatasets().isEmpty());
        assertTrue(deletedSample.getGenerated().isEmpty());
        assertTrue(deletedSample.getContained().isEmpty());
        assertFalse(deletedSample.getProperties().isEmpty());

        // delete
        deleteSample(deletedSample);

        // test successful deletion of sample
        assertNull(sampleDAO.tryGetByTechId(TechId.create(deletedSample)));

        // test successful deletion of sample properties
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

        // Deleted sample should have attachments which prevent it from deletion.
        // Other connections which also prevent sample deletion should be empty in this test.
        assertFalse(deletedSample.getAttachments().isEmpty());
        assertTrue(deletedSample.getDatasets().isEmpty());
        assertTrue(deletedSample.getGenerated().isEmpty());
        assertTrue(deletedSample.getContained().isEmpty());

        // delete
        deleteSample(deletedSample);
    }

    @Test(expectedExceptions = DataIntegrityViolationException.class)
    public final void testDeleteFailWithDatasets()
    {
        final SamplePE deletedSample = findSample("CP-TEST-1", "CISD");

        // Deleted sample should have data sets which prevent it from deletion.
        // Other connections which also prevent sample deletion should be empty in this test.
        assertTrue(deletedSample.getAttachments().isEmpty());
        assertFalse(deletedSample.getDatasets().isEmpty());
        assertTrue(deletedSample.getGenerated().isEmpty());
        assertTrue(deletedSample.getContained().isEmpty());

        // delete
        deleteSample(deletedSample);
    }

    @Test(groups = "broken")
    public final void testDeleteWithGeneratedSamples()
    {
        final SamplePE deletedSample = findSample("3VCP2", "CISD");

        // Deleted sample should have 'generated' samples which prevent it from deletion.
        // Other connections which also prevent sample deletion should be empty in this test.
        assertTrue(deletedSample.getAttachments().isEmpty());
        assertTrue(deletedSample.getDatasets().isEmpty());
        assertFalse(deletedSample.getGenerated().isEmpty());
        assertTrue(deletedSample.getContained().isEmpty());

        // delete
        deleteSample(deletedSample);
    }

    @Test(expectedExceptions = DataIntegrityViolationException.class)
    public final void testDeleteFailWithContainedSamples()
    {
        final SamplePE deletedSample = findSample("C1", "CISD");

        // Deleted sample should have 'contained' samples which prevent it from deletion.
        // Other connections which also prevent sample deletion should be empty in this test.
        assertTrue(deletedSample.getAttachments().isEmpty());
        assertTrue(deletedSample.getDatasets().isEmpty());
        assertTrue(deletedSample.getGenerated().isEmpty());
        assertFalse(deletedSample.getContained().isEmpty());

        // delete
        deleteSample(deletedSample);
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
        samplePE.setSampleType(daoFactory.getSampleTypeDAO()
                .tryFindSampleTypeByCode(DILUTION_PLATE));
        final DatabaseInstancePE homeDatabaseInstance = daoFactory.getHomeDatabaseInstance();
        samplePE.setDatabaseInstance(homeDatabaseInstance);
        samplePE.setRegistrator(getSystemPerson());
        sampleDAO.createSample(samplePE);
        // Following line throws a NonUniqueResultException if sample code not unique.
        sampleDAO.tryFindByCodeAndDatabaseInstance(sampleCode, homeDatabaseInstance);
    }

    //
    // Private methods
    //

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

    private final SampleTypePE getSampleType(final String sampleTypeCode)
    {
        final SampleTypePE sampleType =
                daoFactory.getSampleTypeDAO().tryFindSampleTypeByCode(sampleTypeCode);
        assert sampleType != null;
        return sampleType;
    }

    /**
     * Creates a group sample in the database.
     */
    private final SamplePE createGroupSample()
    {
        final SampleTypePE sampleType = getSampleType(MASTER_PLATE);
        final GroupPE group = createGroup("xxx");
        final SamplePE sample =
                createSample(sampleType, "code", null, null, SampleOwner.createGroup(group));
        save(sample);
        assertNotNull(sample);
        assertNotNull(sample.getSampleType());
        assertNotNull(sample.getGroup());
        return sample;
    }

    private final SamplePE createSample(final SampleTypePE type, final String code)
    {
        final SampleOwner owner =
                SampleOwner.createDatabaseInstance(daoFactory.getHomeDatabaseInstance());
        return createSample(type, code, null, null, owner);
    }

    private final SamplePE createContainedSample(final SampleTypePE type, final String code,
            final SamplePE containerOrNull)
    {
        final SampleOwner owner =
                SampleOwner.createDatabaseInstance(daoFactory.getHomeDatabaseInstance());
        return createSample(type, code, null, containerOrNull, owner);
    }

    private final SamplePE createDerivedSample(final SampleTypePE type, final String code,
            final SamplePE generatorOrNull)
    {
        final SampleOwner owner =
                SampleOwner.createDatabaseInstance(daoFactory.getHomeDatabaseInstance());
        return createSample(type, code, generatorOrNull, null, owner);
    }

    private final SamplePE createSample(final SampleTypePE type, final String code,
            final SamplePE generatorOrNull, final SamplePE containerOrNull,
            final SampleOwner sampleOwner)
    {
        final SamplePE sample = new SamplePE();
        sample.setRegistrator(getSystemPerson());
        sample.setCode(code);
        sample.setPermId(daoFactory.getPermIdDAO().createPermId());
        sample.setSampleType(type);
        sample.setDatabaseInstance(sampleOwner.tryGetDatabaseInstance());
        sample.setGroup(sampleOwner.tryGetGroup());
        if (generatorOrNull != null)
        {
            sample.addParentRelationship(new SampleRelationshipPE(generatorOrNull, sample,
                    daoFactory.getRelationshipTypeDAO().tryFindRelationshipTypeByCode(
                            BasicConstant.PARENT_CHILD_INTERNAL_RELATIONSHIP)));
        }
        sample.setContainer(containerOrNull);
        return sample;
    }

    @Test
    public void testLoadByPermId() throws Exception
    {
        SamplePE sample = daoFactory.getSampleDAO().listAllEntities().get(0);
        HashSet<String> keys = new HashSet<String>();
        keys.add(sample.getPermId());
        keys.add("nonexistent");
        List<SamplePE> result = daoFactory.getSampleDAO().listByPermID(keys);
        AssertJUnit.assertEquals(1, result.size());
        AssertJUnit.assertEquals(sample, result.get(0));
    }

    @Test
    public void testLoadByPermIdNoEntries() throws Exception
    {
        HashSet<String> keys = new HashSet<String>();
        List<SamplePE> result = daoFactory.getSampleDAO().listByPermID(keys);
        AssertJUnit.assertTrue(result.isEmpty());
    }
}
