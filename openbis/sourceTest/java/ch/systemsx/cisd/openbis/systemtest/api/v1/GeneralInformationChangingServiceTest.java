/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.systemtest.api.v1;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationChangingService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.MetaprojectAssignments;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.MetaprojectAssignmentsIds;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.dataset.DataSetCodeId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.experiment.ExperimentIdentifierId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.material.MaterialCodeAndTypeCodeId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.metaproject.MetaprojectIdentifierId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.metaproject.MetaprojectTechIdId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.sample.SampleIdentifierId;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewETPTAssignment;
import ch.systemsx.cisd.openbis.systemtest.PropertyHistory;
import ch.systemsx.cisd.openbis.systemtest.SystemTestCase;
import ch.systemsx.cisd.openbis.util.GeneralInformationServiceUtil;

/**
 * @author Franz-Josef Elmer
 */
@Test(groups = "system test")
public class GeneralInformationChangingServiceTest extends SystemTestCase
{
    @Autowired
    private ICommonServer localCommonServer;

    @Autowired
    private IGeneralInformationService generalInformationService;

    @Autowired
    private IGeneralInformationChangingService generalInformationChangingService;

    private String sessionToken;

    @BeforeMethod
    public void beforeMethod()
    {
        sessionToken = generalInformationService.tryToAuthenticateForAllServices("test", "a");
    }

    @Test
    public void testUpdateExperimentProperties()
    {
    }

    @Test
    public void testUpdateSampleProperties()
    {
        TechId id = new TechId(1043L);
        localCommonServer.assignPropertyType(sessionToken,
                new NewETPTAssignment(EntityKind.SAMPLE, "DESCRIPTION", "CELL_PLATE", false, null,
                        null, 1L, false, false, null, true, false));
        localCommonServer.assignPropertyType(sessionToken, new NewETPTAssignment(EntityKind.SAMPLE,
                "GENDER", "CELL_PLATE", false, null, null, 1L, false, false, null, true, false));
        assertProperties("[ANY_MATERIAL: 2 (GENE), BACTERIUM: BACTERIUM-Y (BACTERIUM), "
                + "COMMENT: extremely simple stuff, ORGANISM: GORILLA, SIZE: 321]",
                localCommonServer.getSampleInfo(sessionToken, id).getParent());
        HashMap<String, String> properties = new HashMap<String, String>();
        properties.put("SIZE", "42");
        properties.put("any_material", "1 (GENE)");
        properties.put("Organism", "DOG");
        properties.put("DESCRIPTION", "hello example");
        properties.put("gender", "FEMALE");

        generalInformationChangingService.updateSampleProperties(sessionToken, id.getId(),
                properties);

        assertProperties("[ANY_MATERIAL: 1 (GENE), BACTERIUM: BACTERIUM-Y (BACTERIUM), "
                + "COMMENT: extremely simple stuff, DESCRIPTION: hello example, GENDER: FEMALE, "
                + "ORGANISM: DOG, SIZE: 42]", localCommonServer.getSampleInfo(sessionToken, id)
                .getParent());

        List<PropertyHistory> history = getSamplePropertiesHistory(id.getId());
        assertEquals(
                "[ANY_MATERIAL: material:2 [GENE]<a:1>, ORGANISM: term:GORILLA [ORGANISM]<a:1>, SIZE: 321<a:1>]",
                history.toString());
    }

    @Test
    public void testCreateMetaproject()
    {
        String name = "BRAND_NEW_METAPROJECT";
        String description = "I'm brand new";

        List<String> beforeNames = getUtil().listMetaprojectNames(sessionToken);
        assertFalse(beforeNames.contains(name));

        Metaproject metaproject =
                generalInformationChangingService
                        .createMetaproject(sessionToken, name, description);

        List<String> afterNames = getUtil().listMetaprojectNames(sessionToken);
        assertTrue(afterNames.contains(name));

        assertEquals(beforeNames.size() + 1, afterNames.size());
        afterNames.remove(name);
        assertEquals(beforeNames, afterNames);

        assertNotNull(metaproject.getId());
        assertEquals("test", metaproject.getOwnerId());
        assertEquals("/test/" + name, metaproject.getIdentifier());
        assertEquals(name, metaproject.getName());
        assertEquals(description, metaproject.getDescription());
        assertNotNull(metaproject.getCreationDate());
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void testCreateMetaprojectWithNameDuplicatedForSameOwner()
    {
        generalInformationChangingService.createMetaproject(sessionToken, "TEST_METAPROJECTS",
                "My name is already used by the same owner");
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void testCreateMetaprojectWithNameThatIsUsedInDifferentCaseForSameOwner()
    {
        generalInformationChangingService.createMetaproject(sessionToken, "TEST_metaprojects",
                "My name is already used by the same owner but in a different case");
    }

    @Test
    public void testCreateMetaprojectWithNameDuplicatedForDifferentOwner()
    {
        generalInformationChangingService.createMetaproject(sessionToken, "TEST_METAPROJECTS_2",
                "My name is already used by a different owner");
    }

    @Test
    public void testCreateMetaprojectWithNameThatIsUsedInDifferentCaseForDifferentOwner()
    {
        generalInformationChangingService.createMetaproject(sessionToken, "TEST_metaprojects_2",
                "My name is already used by a different owner in a different case");
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void testCreateMetaprojectWithEmptyName()
    {
        generalInformationChangingService.createMetaproject(sessionToken, null, "My name is empty");
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void testCreateMetaprojectWithNameThatContainsDisallowedCharacters()
    {
        generalInformationChangingService.createMetaproject(sessionToken,
                "THIS NAME IS DISALLOWED", "My name is not allowed");
    }

    @Test
    public void testCreateMetaprojectWithNameThatUsesVariedCaseMaintainsTheCase()
    {
        String name = "NameWithVariedCase";

        Metaproject metaproject =
                generalInformationChangingService.createMetaproject(sessionToken, name,
                        "My name uses varied case");
        assertEquals(name, metaproject.getName());
        List<String> names = getUtil().listMetaprojectNames(sessionToken);
        assertTrue(names.contains(name));
    }

    @Test
    public void testCreateMetaprojectWithEmptyDescription()
    {
        String name = "METAPROJECT_WITH_EMPTY_DESCRIPTION";

        Metaproject metaproject =
                generalInformationChangingService.createMetaproject(sessionToken, name, null);
        assertEquals(name, metaproject.getName());
        assertNull(metaproject.getDescription());
    }

    @Test
    public void testUpdateMetaproject()
    {
        String beforeName = "TEST_METAPROJECTS";
        String afterName = "TEST_METAPROJECTS_UPDATED";
        String description = "My description is brand new";

        List<String> beforeNames = getUtil().listMetaprojectNames(sessionToken);
        assertTrue(beforeNames.contains(beforeName));
        assertFalse(beforeNames.contains(afterName));

        Metaproject metaproject =
                generalInformationChangingService.updateMetaproject(sessionToken,
                        new MetaprojectIdentifierId("/test/" + beforeName), afterName, description);

        List<String> afterNames = getUtil().listMetaprojectNames(sessionToken);
        assertFalse(afterNames.contains(beforeName));
        assertTrue(afterNames.contains(afterName));

        assertEquals(beforeNames.size(), afterNames.size());
        beforeNames.remove(beforeName);
        afterNames.remove(afterName);
        assertEquals(beforeNames, afterNames);

        assertNotNull(metaproject.getId());
        assertEquals("test", metaproject.getOwnerId());
        assertEquals("/test/" + afterName, metaproject.getIdentifier());
        assertEquals(afterName, metaproject.getName());
        assertEquals(description, metaproject.getDescription());
        assertNotNull(metaproject.getCreationDate());
    }

    @Test(expectedExceptions = AuthorizationFailureException.class)
    public void testUpdateMetaprojectOwnedBySomebodyElse()
    {
        generalInformationChangingService.updateMetaproject(sessionToken,
                new MetaprojectIdentifierId("/test_role/TEST_METAPROJECTS"), "SOME_NEW_NAME",
                "some new description");
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void testUpdateMetaprojectWithEmptyId()
    {
        generalInformationChangingService.updateMetaproject(sessionToken, null, "SOME_NEW_NAME",
                "some new description");
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void testUpdateMetaprojectThatDoesntExist()
    {
        generalInformationChangingService.updateMetaproject(sessionToken,
                new MetaprojectIdentifierId("/test/METAPROJECT_THAT_DOESNT_EXIST"),
                "SOME_NEW_NAME", "some new description");
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void testUpdateMetaprojectToEmptyName()
    {
        generalInformationChangingService.updateMetaproject(sessionToken,
                new MetaprojectIdentifierId("/test/TEST_METAPROJECTS"), null,
                "some new description");
    }

    @Test
    public void testUpdateMetaprojectToEmptyDescription()
    {
        generalInformationChangingService.updateMetaproject(sessionToken,
                new MetaprojectIdentifierId("/test/TEST_METAPROJECTS"), "TEST_METAPROJECTS", null);
    }

    @Test
    public void testDeleteMetaproject()
    {
        String name = "TEST_METAPROJECTS";

        List<String> beforeNames = getUtil().listMetaprojectNames(sessionToken);
        assertTrue(beforeNames.contains(name));

        generalInformationChangingService.deleteMetaproject(sessionToken,
                new MetaprojectIdentifierId("/test/" + name));

        List<String> afterNames = getUtil().listMetaprojectNames(sessionToken);
        assertFalse(afterNames.contains(name));

        assertEquals(beforeNames.size() - 1, afterNames.size());
        beforeNames.remove(name);
        assertEquals(beforeNames, afterNames);
    }

    @Test(expectedExceptions = AuthorizationFailureException.class)
    public void testDeleteMetaprojectOwnedBySomebodyElse()
    {
        generalInformationChangingService.deleteMetaproject(sessionToken,
                new MetaprojectIdentifierId("/test_role/TEST_METAPROJECTS"));
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void testDeleteMetaprojectWithEmptyId()
    {
        generalInformationChangingService.deleteMetaproject(sessionToken, null);
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void testDeleteMetaprojectThatDoesntExist()
    {
        generalInformationChangingService.deleteMetaproject(sessionToken,
                new MetaprojectIdentifierId("/test/METAPROJECT_THAT_DOESNT_EXIST"));
    }

    @Test
    public void testAddToMetaproject()
    {
        Metaproject metaproject = getUtil().createMetaprojectWithAssignments(sessionToken);

        MetaprojectAssignments assignments =
                generalInformationService.getMetaproject(sessionToken, new MetaprojectIdentifierId(
                        metaproject.getIdentifier()));

        List<Long> experimentIds = getUtil().getObjectIds(assignments.getExperiments());
        assertEquals(3, experimentIds.size());
        assertTrue(experimentIds.contains(2L));
        assertTrue(experimentIds.contains(22L));
        assertTrue(experimentIds.contains(23L));

        List<Long> samplesIds = getUtil().getObjectIds(assignments.getSamples());
        assertEquals(4, samplesIds.size());
        assertTrue(samplesIds.contains(647L));
        assertTrue(samplesIds.contains(602L));
        assertTrue(samplesIds.contains(340L));
        assertTrue(samplesIds.contains(342L));

        List<Long> dataSetIds = getUtil().getObjectIds(assignments.getDataSets());
        assertEquals(2, dataSetIds.size());
        assertTrue(dataSetIds.contains(8L));
        assertTrue(dataSetIds.contains(12L));

        List<Long> materialIds = getUtil().getObjectIds(assignments.getMaterials());
        assertEquals(2, materialIds.size());
        assertTrue(materialIds.contains(18L));
        assertTrue(materialIds.contains(8L));
    }

    @Test(expectedExceptions = AuthorizationFailureException.class)
    public void testAddToMetaprojectOwnedBySomebodyElse()
    {
        generalInformationChangingService.addToMetaproject(sessionToken,
                new MetaprojectIdentifierId("/test_role/TEST_METAPROJECTS"),
                new MetaprojectAssignmentsIds());
    }

    @Test
    public void testAddToMetaprojectAssignmentsThatAlreadyExist()
    {
        Metaproject metaproject =
                generalInformationChangingService.createMetaproject(sessionToken,
                        "BRAND_NEW_METAPROJECT", null);

        MetaprojectAssignmentsIds assignmentsToAdd = new MetaprojectAssignmentsIds();
        assignmentsToAdd.addExperiment(new ExperimentIdentifierId("/CISD/NEMO/EXP1"));

        generalInformationChangingService.addToMetaproject(sessionToken, new MetaprojectTechIdId(
                metaproject.getId()), assignmentsToAdd);
        generalInformationChangingService.addToMetaproject(sessionToken, new MetaprojectTechIdId(
                metaproject.getId()), assignmentsToAdd);

        MetaprojectAssignments assignments =
                generalInformationService.getMetaproject(sessionToken, new MetaprojectTechIdId(
                        metaproject.getId()));
        assertEquals(1, assignments.getExperiments().size());
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void testAddToMetaprojectWithEmptyId()
    {
        generalInformationChangingService.addToMetaproject(sessionToken, null,
                new MetaprojectAssignmentsIds());
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void testAddToMetaprojectWithNullAssignments()
    {
        generalInformationChangingService.addToMetaproject(sessionToken,
                new MetaprojectIdentifierId("/test/TEST_METAPROJECTS"), null);
    }

    @Test
    public void testAddToMetaprojectWithEmptyAssignments()
    {
        generalInformationChangingService.addToMetaproject(sessionToken,
                new MetaprojectIdentifierId("/test/TEST_METAPROJECTS"),
                new MetaprojectAssignmentsIds());
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void testAddToMetaprojectThatDoesntExist()
    {
        generalInformationChangingService.addToMetaproject(sessionToken,
                new MetaprojectIdentifierId("/test/METAPROJECT_THAT_DOESNT_EXIST"),
                new MetaprojectAssignmentsIds());
    }

    @Test
    public void testRemoveFromMetaproject()
    {
        Metaproject metaproject = getUtil().createMetaprojectWithAssignments(sessionToken);

        MetaprojectAssignmentsIds assignmentsToRemove = new MetaprojectAssignmentsIds();
        assignmentsToRemove.addExperiment(new ExperimentIdentifierId("/CISD/NEMO/EXP1"));
        assignmentsToRemove.addSample(new SampleIdentifierId("/A03"));
        assignmentsToRemove.addDataSet(new DataSetCodeId("20081105092259000-8"));
        assignmentsToRemove.addMaterial(new MaterialCodeAndTypeCodeId("GFP", "CONTROL"));

        generalInformationChangingService.removeFromMetaproject(sessionToken,
                new MetaprojectIdentifierId(metaproject.getIdentifier()), assignmentsToRemove);

        MetaprojectAssignments assignments =
                generalInformationService.getMetaproject(sessionToken, new MetaprojectIdentifierId(
                        metaproject.getIdentifier()));

        List<Long> experimentIds = getUtil().getObjectIds(assignments.getExperiments());
        assertEquals(2, experimentIds.size());
        assertTrue(experimentIds.contains(22L));
        assertTrue(experimentIds.contains(23L));

        List<Long> samplesIds = getUtil().getObjectIds(assignments.getSamples());
        assertEquals(3, samplesIds.size());
        assertTrue(samplesIds.contains(602L));
        assertTrue(samplesIds.contains(340L));
        assertTrue(samplesIds.contains(342L));

        List<Long> dataSetIds = getUtil().getObjectIds(assignments.getDataSets());
        assertEquals(1, dataSetIds.size());
        assertTrue(dataSetIds.contains(12L));

        List<Long> materialIds = getUtil().getObjectIds(assignments.getMaterials());
        assertEquals(1, materialIds.size());
        assertTrue(materialIds.contains(8L));
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void testRemoveFromMetaprojectOwnedBySomebodyElse()
    {
        generalInformationChangingService.removeFromMetaproject(sessionToken,
                new MetaprojectIdentifierId("/test_role/TEST_METAPROJECTS"),
                new MetaprojectAssignmentsIds());
    }

    public void testRemoveFromMetaprojectAssignmentsThatDoNotExist()
    {
        Metaproject metaproject = getUtil().createMetaprojectWithAssignments(sessionToken);
        MetaprojectAssignmentsIds assignmentsToRemove = new MetaprojectAssignmentsIds();
        assignmentsToRemove.addExperiment(new ExperimentIdentifierId("/CISD/NEMO/EXP10"));

        generalInformationChangingService.removeFromMetaproject(sessionToken,
                new MetaprojectIdentifierId(metaproject.getIdentifier()), assignmentsToRemove);
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void testRemoveFromMetaprojectWithEmptyId()
    {
        generalInformationChangingService.removeFromMetaproject(sessionToken, null,
                new MetaprojectAssignmentsIds());
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void testRemoveFromMetaprojectWithNullAssignments()
    {
        generalInformationChangingService.removeFromMetaproject(sessionToken,
                new MetaprojectIdentifierId("/test/TEST_METAPROJECTS"), null);
    }

    @Test
    public void testRemoveFromMetaprojectWithEmptyAssignments()
    {
        generalInformationChangingService.removeFromMetaproject(sessionToken,
                new MetaprojectIdentifierId("/test/TEST_METAPROJECTS"),
                new MetaprojectAssignmentsIds());
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void testRemoveFromMetaprojectThatDoesntExist()
    {
        generalInformationChangingService.removeFromMetaproject(sessionToken,
                new MetaprojectIdentifierId("/test/METAPROJECT_THAT_DOESNT_EXIST"),
                new MetaprojectAssignmentsIds());
    }

    private GeneralInformationServiceUtil getUtil()
    {
        return new GeneralInformationServiceUtil(generalInformationService,
                generalInformationChangingService);
    }

}
