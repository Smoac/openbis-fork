/*
 * Copyright 2008 ETH Zuerich, CISD
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityPropertyTypeDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermWithStats;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * Test cases for {@link EntityPropertyTypeDAO}.
 * 
 * @author Izabela Adamczyk
 */
@Test(groups =
    { "db", "property" })
public class EntityPropertyTypeDAOTest extends AbstractDAOTest
{

    @Test(dataProvider = "entityKindsWithEntityTypeAndPropertyTypeMandatory")
    public final void testTryFindAssignment(EntityKind entityKind, String typeCode,
            String propertyCode)
    {
        EntityTypePropertyTypePE assignment =
                tryToGetAssignment(entityKind, typeCode, propertyCode);
        Assert.assertEquals(true, assignment.isMandatory());
    }

    @Test(dataProvider = "entityKindsWithEntityTypeAndPropertyTypeNotAssigned")
    public final void testTryFindNonexistentAssignment(EntityKind entityKind, String typeCode,
            String propertyCode)
    {
        EntityTypePropertyTypePE assignment =
                tryToGetAssignment(entityKind, typeCode, propertyCode);
        Assert.assertNull(assignment);
    }

    @Test(dataProvider = "entityKindsWithEntityTypeAndPropertyTypeNotAssigned")
    public void testCreateEntityPropertyTypeAssignment(EntityKind entityKind, String typeCode,
            String propertyCode)
    {
        // prepare data
        EntityTypePE entityType =
                daoFactory.getEntityTypeDAO(entityKind).tryToFindEntityTypeByCode(typeCode);
        PropertyTypePE propertyType =
                daoFactory.getPropertyTypeDAO().tryFindPropertyTypeByCode(propertyCode);
        // check assignment does not exist
        Assert.assertNull(daoFactory.getEntityPropertyTypeDAO(entityKind).tryFindAssignment(
                entityType, propertyType));
        // create assignment
        EntityTypePropertyTypePE entityPropertyTypeAssignement =
                createAssignment(entityKind, entityType, propertyType);
        daoFactory.getEntityPropertyTypeDAO(entityKind).createEntityPropertyTypeAssignment(
                entityPropertyTypeAssignement);
        // check assignment exists
        Assert.assertNotNull(daoFactory.getEntityPropertyTypeDAO(entityKind).tryFindAssignment(
                entityType, propertyType));

    }

    @Test
    public void testListEntities() throws Exception
    {
        EntityKind entityKind = EntityKind.EXPERIMENT;
        String typeCode = "SIRNA_HCS";
        EntityTypePE entityType =
                daoFactory.getEntityTypeDAO(entityKind).tryToFindEntityTypeByCode(typeCode);
        List<ExperimentPE> allExperiments = daoFactory.getExperimentDAO().listExperiments();
        Assert.assertEquals(new HashSet<ExperimentPE>(filter(allExperiments, entityType)),
                new HashSet<IEntityPropertiesHolder>(daoFactory
                        .getEntityPropertyTypeDAO(entityKind).listEntities(entityType)));
    }

    private static List<ExperimentPE> filter(List<ExperimentPE> allExperiments,
            EntityTypePE entityType)
    {
        List<ExperimentPE> result = new ArrayList<ExperimentPE>();
        for (ExperimentPE experimentPE : allExperiments)
        {
            if (experimentPE.getExperimentType().getCode().equals(entityType.getCode()))
            {
                result.add(experimentPE);
            }
        }
        return result;
    }

    public final void testCountTermUsageStatistics()
    {
        IEntityPropertyTypeDAO dao = daoFactory.getEntityPropertyTypeDAO(EntityKind.EXPERIMENT);
        VocabularyPE vocabulary = daoFactory.getVocabularyDAO().tryFindVocabularyByCode("GENDER");
        assert vocabulary != null : "gender vocabulary not found";

        VocabularyTermWithStats maleTermWithStats = createTermWithStats(vocabulary, "MALE");
        VocabularyTermWithStats femaleTermWithStats = createTermWithStats(vocabulary, "FEMALE");

        List<VocabularyTermWithStats> termsWithStats =
                Arrays.asList(maleTermWithStats, femaleTermWithStats);
        dao.fillTermUsageStatistics(termsWithStats, vocabulary);

        assertEquals(3, maleTermWithStats.getUsageCounter(EntityKind.EXPERIMENT));
        assertEquals(1, femaleTermWithStats.getUsageCounter(EntityKind.EXPERIMENT));
    }

    private VocabularyTermWithStats createTermWithStats(VocabularyPE vocabulary, String termCode)
    {
        final VocabularyTermPE term = tryFindTerm(vocabulary, termCode);
        assert term != null : "term " + termCode + " not found in " + vocabulary.getCode()
                + " vocabulary";
        return new VocabularyTermWithStats(term);
    }

    private static VocabularyTermPE tryFindTerm(VocabularyPE vocabulary, String termCode)
    {
        Set<VocabularyTermPE> terms = vocabulary.getTerms();
        for (VocabularyTermPE term : terms)
        {
            if (term.getCode().equalsIgnoreCase(termCode))
            {
                return term;
            }
        }
        return null;
    }

    @Test
    public void testListPropertiesByVocabularyTerm()
    {
        IEntityPropertyTypeDAO entityPropertyTypeDAO =
                daoFactory.getEntityPropertyTypeDAO(EntityKind.MATERIAL);
        List<EntityPropertyPE> properties =
                entityPropertyTypeDAO.listPropertiesByVocabularyTerm("FLY");

        assertEquals(1, properties.size());
        assertEquals("FLY", properties.get(0).getVocabularyTerm().getCode());
    }

    @Test(groups = "broken-deletion")
    // FIXME LMS-2440
    public void testDelete()
    {
        EntityTypePropertyTypePE assignment =
                tryToGetAssignment(EntityKind.EXPERIMENT, "SIRNA_HCS", "DESCRIPTION");
        assertEquals(false, assignment.getPropertyValues().isEmpty());
        ExperimentPropertyPE propertyValue =
                (ExperimentPropertyPE) (assignment.getPropertyValues().iterator().next());
        ExperimentPE experiment = propertyValue.getEntity();
        long id = experiment.getId();
        int totalProps = experiment.getProperties().size();

        daoFactory.getEntityPropertyTypeDAO(EntityKind.EXPERIMENT).delete(assignment);

        // load the experiment once again - it was cleared from session
        experiment = new ExperimentPE();
        daoFactory.getSessionFactory().getCurrentSession().load(experiment, id);
        assertEquals(totalProps - 1, experiment.getProperties().size());
    }

    private EntityTypePropertyTypePE tryToGetAssignment(EntityKind entityKind,
            String entityTypeCode, String propertyTypeCode)
    {
        EntityTypePE entityType =
                daoFactory.getEntityTypeDAO(entityKind).tryToFindEntityTypeByCode(entityTypeCode);
        PropertyTypePE propertyType =
                daoFactory.getPropertyTypeDAO().tryFindPropertyTypeByCode(propertyTypeCode);
        EntityTypePropertyTypePE assignment =
                daoFactory.getEntityPropertyTypeDAO(entityKind).tryFindAssignment(entityType,
                        propertyType);
        return assignment;
    }

    @SuppressWarnings("unused")
    @DataProvider
    private final static Object[][] entityKindsWithEntityTypeAndPropertyTypeMandatory()
    {
        return new Object[][]
            {
                { EntityKind.EXPERIMENT, "SIRNA_HCS", "DESCRIPTION" },
                { EntityKind.SAMPLE, "CONTROL_LAYOUT", "$PLATE_GEOMETRY" },
                { EntityKind.MATERIAL, "BACTERIUM", "DESCRIPTION" } };
    }

    @SuppressWarnings("unused")
    @DataProvider
    private final static Object[][] entityKindsWithEntityTypeAndPropertyTypeNotAssigned()
    {
        return new Object[][]
            {
                { EntityKind.EXPERIMENT, "SIRNA_HCS", "IS_VALID" },
                { EntityKind.SAMPLE, "CONTROL_LAYOUT", "IS_VALID" },
                { EntityKind.MATERIAL, "BACTERIUM", "IS_VALID" }, };
    }
}
