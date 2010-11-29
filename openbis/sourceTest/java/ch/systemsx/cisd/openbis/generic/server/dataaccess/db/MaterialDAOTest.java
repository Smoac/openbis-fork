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

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.Assert;

import org.springframework.dao.DataIntegrityViolationException;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IMaterialDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * Test cases for corresponding {@link MaterialDAO} class.
 * 
 * @author Izabela Adamczyk
 */
@Test(groups =
    { "db", "material" })
@SuppressWarnings("deprecation")
public final class MaterialDAOTest extends AbstractDAOTest
{
    private static final String BACTERIUM = "BACTERIUM";

    private static final String BRAND_NEW_BACTERIUM = "BRAND_NEW_BACTERIUM";

    private final int NUMBER_OF_BACTERIA = 4;

    @Test
    public void testCreateMaterials() throws Exception
    {
        MaterialTypePE type =
                (MaterialTypePE) daoFactory.getEntityTypeDAO(EntityKind.MATERIAL)
                        .tryToFindEntityTypeByCode(BACTERIUM);
        List<MaterialPE> bacteria_before =
                daoFactory.getMaterialDAO().listMaterialsWithProperties(type);
        Assert.assertEquals(NUMBER_OF_BACTERIA, bacteria_before.size());
        List<MaterialPE> newMaterials = new ArrayList<MaterialPE>();
        newMaterials.add(createMaterial(type, "BRAND_NEW_BACTERIUM_1"));
        newMaterials.add(createMaterial(type, "BRAND_NEW_BACTERIUM_2"));
        Collections.sort(newMaterials);
        daoFactory.getMaterialDAO().createMaterials(newMaterials);
        List<MaterialPE> bacteria_after =
                daoFactory.getMaterialDAO().listMaterialsWithProperties(type);
        Assert.assertEquals(NUMBER_OF_BACTERIA + newMaterials.size(), bacteria_after.size());
        bacteria_after.removeAll(bacteria_before);
        Collections.sort(bacteria_after);
        for (int i = 0; i < newMaterials.size(); i++)
        {
            Assert.assertEquals(newMaterials.get(i), bacteria_after.get(i));
        }
    }

    @Test(expectedExceptions = DataIntegrityViolationException.class)
    public void testFailCreateMaterialsWithTheSameCode() throws Exception
    {
        MaterialTypePE type =
                (MaterialTypePE) daoFactory.getEntityTypeDAO(EntityKind.MATERIAL)
                        .tryToFindEntityTypeByCode(BACTERIUM);
        List<MaterialPE> newMaterials = new ArrayList<MaterialPE>();
        newMaterials.add(createMaterial(type, BRAND_NEW_BACTERIUM));
        newMaterials.add(createMaterial(type, BRAND_NEW_BACTERIUM));
        daoFactory.getMaterialDAO().createMaterials(newMaterials);
    }

    @Test(expectedExceptions = DataIntegrityViolationException.class)
    public void testFailCreateMaterialsWithExistingCode() throws Exception
    {
        MaterialTypePE type =
                (MaterialTypePE) daoFactory.getEntityTypeDAO(EntityKind.MATERIAL)
                        .tryToFindEntityTypeByCode(BACTERIUM);
        List<MaterialPE> bacteria_before =
                daoFactory.getMaterialDAO().listMaterialsWithProperties(type);
        String existingBacteriumCode = bacteria_before.get(0).getCode();
        List<MaterialPE> newMaterials = new ArrayList<MaterialPE>();
        newMaterials.add(createMaterial(type, existingBacteriumCode));
        daoFactory.getMaterialDAO().createMaterials(newMaterials);
    }

    @Test
    public final void testDeleteWithProperties()
    {
        final IMaterialDAO materialDAO = daoFactory.getMaterialDAO();

        // BACTERIUM2 has not been used as a property value
        MaterialIdentifier identifier = new MaterialIdentifier("BACTERIUM2", "BACTERIUM");
        final MaterialPE deletedMaterial = materialDAO.tryFindMaterial(identifier);
        assertNotNull(deletedMaterial);
        assertFalse(deletedMaterial.getProperties().isEmpty());

        materialDAO.delete(deletedMaterial);

        assertNull(materialDAO.tryFindMaterial(identifier));

        List<EntityTypePropertyTypePE> retrievedPropertyTypes =
                daoFactory.getEntityPropertyTypeDAO(EntityKind.MATERIAL).listEntityPropertyTypes(
                        deletedMaterial.getEntityType());
        for (MaterialPropertyPE property : deletedMaterial.getProperties())
        {
            int index = retrievedPropertyTypes.indexOf(property.getEntityTypePropertyType());
            EntityTypePropertyTypePE retrievedPropertyType = retrievedPropertyTypes.get(index);
            assertFalse(retrievedPropertyType.getPropertyValues().contains(property));
        }
    }

    @Test(expectedExceptions = DataIntegrityViolationException.class)
    public final void testFailDeleteMaterialUsedAsPropertyValue()
    {
        final IMaterialDAO materialDAO = daoFactory.getMaterialDAO();

        String bacteriumX = "BACTERIUM-X";
        MaterialIdentifier identifier = new MaterialIdentifier(bacteriumX, BACTERIUM);
        final MaterialPE usedMaterial = materialDAO.tryFindMaterial(identifier);
        assertNotNull(usedMaterial);

        // Assert that BACTERIUM-X has been used as a property value
        SamplePE sample =
                daoFactory.getSampleDAO().tryFindByCodeAndSpace("CP-TEST-1", createSpace("CISD"));
        assertNotNull(sample);
        boolean bacteriumFound = false;
        for (SamplePropertyPE property : sample.getProperties())
        {
            MaterialPE materialValue = property.getMaterialValue();
            if (materialValue != null && materialValue.getCode().equals(bacteriumX)
                    && materialValue.getMaterialType().getCode().equals(BACTERIUM))
            {
                bacteriumFound = true;
            }
        }
        assertTrue(bacteriumFound);

        materialDAO.delete(usedMaterial);
    }
}