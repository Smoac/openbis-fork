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
import java.util.Date;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityPropertyTypeDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;
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
        EntityTypePE entityType =
                daoFactory.getEntityTypeDAO(entityKind).tryToFindEntityTypeByCode(typeCode);
        PropertyTypePE propertyType =
                daoFactory.getPropertyTypeDAO().tryFindPropertyTypeByCode(propertyCode);
        EntityTypePropertyTypePE assignment =
                daoFactory.getEntityPropertyTypeDAO(entityKind).tryFindAssignment(entityType,
                        propertyType);
        Assert.assertEquals(true, assignment.isMandatory());
    }

    @Test(dataProvider = "entityKindsWithEntityTypeAndPropertyTypeNotAssigned")
    public final void testTryFindNonexistentAssignment(EntityKind entityKind, String typeCode,
            String propertyCode)
    {
        EntityTypePE entityType =
                daoFactory.getEntityTypeDAO(entityKind).tryToFindEntityTypeByCode(typeCode);
        PropertyTypePE propertyType =
                daoFactory.getPropertyTypeDAO().tryFindPropertyTypeByCode(propertyCode);
        EntityTypePropertyTypePE assignment =
                daoFactory.getEntityPropertyTypeDAO(entityKind).tryFindAssignment(entityType,
                        propertyType);
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
        Assert.assertEquals(filter(allExperiments, entityType), daoFactory
                .getEntityPropertyTypeDAO(entityKind).listEntities(entityType));
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

    private EntityTypePropertyTypePE createAssignment(EntityKind entityKind,
            EntityTypePE entityType, PropertyTypePE propertyType)
    {
        final PersonPE registrator = getTestPerson();
        EntityTypePropertyTypePE result =
                EntityTypePropertyTypePE.createEntityTypePropertyType(entityKind);
        result.setEntityType(entityType);
        result.setPropertyType(propertyType);
        result.setRegistrator(registrator);
        result.setRegistrationDate(new Date());
        return result;
    }

    public final void testCountTermUsageStatistics()
    {
        IEntityPropertyTypeDAO dao = daoFactory.getEntityPropertyTypeDAO(EntityKind.EXPERIMENT);
        VocabularyPE vocabulary =
                daoFactory.getVocabularyDAO().tryFindVocabularyByCode("USER.GENDER");
        assert vocabulary != null : "gender vocabulary not found";
        VocabularyTermPE term = tryFindTerm(vocabulary, "MALE");
        assert term != null : "term MALE not found in GENDER vocabulary";
        long stats = dao.countTermUsageStatistics(term);
        assertEquals(3, stats);
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

    @SuppressWarnings("unused")
    @DataProvider
    private final static Object[][] entityKindsWithEntityTypeAndPropertyTypeMandatory()
    {
        return new Object[][]
            {
                { EntityKind.EXPERIMENT, "SIRNA_HCS", "USER.DESCRIPTION" },
                { EntityKind.SAMPLE, "CONTROL_LAYOUT", "PLATE_GEOMETRY" },
                { EntityKind.MATERIAL, "BACTERIUM", "USER.DESCRIPTION" } };
    }

    @SuppressWarnings("unused")
    @DataProvider
    private final static Object[][] entityKindsWithEntityTypeAndPropertyTypeNotAssigned()
    {
        return new Object[][]
            {
                { EntityKind.EXPERIMENT, "SIRNA_HCS", "USER.IS_VALID" },
                { EntityKind.SAMPLE, "CONTROL_LAYOUT", "USER.IS_VALID" },
                { EntityKind.MATERIAL, "BACTERIUM", "USER.IS_VALID" }, };
    }
}
