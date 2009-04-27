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
import static org.testng.AssertJUnit.assertNotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import junit.framework.Assert;

import org.springframework.dao.DataIntegrityViolationException;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IMaterialDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityDataType;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * Test cases for corresponding {@link EntityTypeDAO} class.
 * 
 * @author Christian Ribeaud
 */
@Test(groups =
    { "db", "materialType" })
public final class EntityTypeDAOTest extends AbstractDAOTest
{

    private static final String MATERIAL = "MATERIAL";

    private static final String MATERIAL_TYPE = "material-type";

    static final void checkEntityType(final EntityTypePE entityType, final String entityTypeCode)
    {
        assertNotNull(entityType);
        if (entityTypeCode != null)
        {
            assertEquals(entityTypeCode, entityType.getCode());
        } else
        {
            assertNotNull(entityType.getCode());
        }
        assertNotNull(entityType.getDescription());
        assertNotNull(entityType.getId());
    }

    static final List<EntityTypePE> listSortedEntyTypes(final IEntityTypeDAO entityTypeDAO)
    {
        final List<EntityTypePE> entityTypes = entityTypeDAO.listEntityTypes();
        Collections.sort(entityTypes, new Comparator<EntityTypePE>()
            {
                //
                // Comparator
                //

                public final int compare(final EntityTypePE o1, final EntityTypePE o2)
                {
                    return o1.getCode().compareTo(o2.getCode());
                }
            });
        return entityTypes;
    }

    @Test
    public final void testListMaterialTypes()
    {
        final IEntityTypeDAO entityTypeDAO = daoFactory.getEntityTypeDAO(EntityKind.MATERIAL);
        assertEquals(7, entityTypeDAO.listEntityTypes().size());
    }

    @Test(dependsOnMethods = "testListMaterialTypes")
    public final void testFindMaterialTypeByCode()
    {
        final IEntityTypeDAO entityTypeDAO = daoFactory.getEntityTypeDAO(EntityKind.MATERIAL);
        final EntityTypePE materialType = entityTypeDAO.listEntityTypes().get(0);
        final String code = materialType.getCode();
        final EntityTypePE foundType = entityTypeDAO.tryToFindEntityTypeByCode(code);

        checkEntityType(foundType, code);
        assertEquals(materialType.getId(), foundType.getId());
        assertEquals(code, foundType.getCode());
        assertEquals(materialType.getDescription(), foundType.getDescription());
    }

    @Test
    public final void testCreateMaterialType()
    {
        final IEntityTypeDAO materialTypeDAO = daoFactory.getEntityTypeDAO(EntityKind.MATERIAL);
        final int sizeBefore = materialTypeDAO.listEntityTypes().size();
        MaterialTypePE entityType = createMaterialType("material-girl");
        materialTypeDAO.createOrUpdateEntityType(entityType);
        final int sizeAfter = materialTypeDAO.listEntityTypes().size();
        assertEquals(sizeBefore + 1, sizeAfter);

        EntityTypePE foundEntityType =
                materialTypeDAO.tryToFindEntityTypeByCode(entityType.getCode());
        assertEquals(entityType, foundEntityType);
    }

    @Test
    public final void testDeleteUnusedMaterialType()
    {
        final IEntityTypeDAO materialTypeDAO = daoFactory.getEntityTypeDAO(EntityKind.MATERIAL);
        MaterialTypePE entityType = createMaterialType(MATERIAL_TYPE);
        materialTypeDAO.createOrUpdateEntityType(entityType);
        int sizeBeforeDeletion = materialTypeDAO.listEntityTypes().size();
        materialTypeDAO.deleteEntityType(entityType);
        assertEquals(sizeBeforeDeletion - 1, materialTypeDAO.listEntityTypes().size());
    }

    @Test
    public final void testFailDeleteMaterialTypeUsedByMaterials()
    {
        final IEntityTypeDAO materialTypeDAO = daoFactory.getEntityTypeDAO(EntityKind.MATERIAL);
        MaterialTypePE materialType = createMaterialType(MATERIAL_TYPE);
        materialTypeDAO.createOrUpdateEntityType(materialType);
        final IMaterialDAO materialDAO = daoFactory.getMaterialDAO();
        List<MaterialPE> materials = createMaterials(3, MATERIAL, materialType);
        materialDAO.createMaterials(materials);
        boolean exceptionThrown = false;
        try
        {
            materialTypeDAO.deleteEntityType(materialType);
        } catch (DataIntegrityViolationException e)
        {
            exceptionThrown = true;
        }
        Assert.assertTrue(exceptionThrown);
    }

    @Test
    public final void testFailDeleteMaterialTypeUsedInPropertyType()
    {
        final IEntityTypeDAO materialTypeDAO = daoFactory.getEntityTypeDAO(EntityKind.MATERIAL);
        MaterialTypePE materialType = createMaterialType(MATERIAL_TYPE);
        materialTypeDAO.createOrUpdateEntityType(materialType);
        PropertyTypePE materialPropertyType = createMaterialPropertyType(materialType);
        daoFactory.getPropertyTypeDAO().createPropertyType(materialPropertyType);
        boolean exceptionThrown = false;
        try
        {
            materialTypeDAO.deleteEntityType(materialType);
        } catch (DataIntegrityViolationException e)
        {
            exceptionThrown = true;
        }
        Assert.assertTrue(exceptionThrown);
    }

    private PropertyTypePE createMaterialPropertyType(MaterialTypePE materialType)
    {
        return createPropertyType(daoFactory.getPropertyTypeDAO().getDataTypeByCode(
                EntityDataType.MATERIAL), "USER.MATERIAL-PROPERTY-TYPE", null, materialType);
    }

    @Test
    public final void testDeleteMaterialTypeWithAssignedPropertyType()
    {
        final IEntityTypeDAO materialTypeDAO = daoFactory.getEntityTypeDAO(EntityKind.MATERIAL);
        MaterialTypePE materialType = createMaterialType(MATERIAL_TYPE);
        materialTypeDAO.createOrUpdateEntityType(materialType);
        assignPropertyType(materialType, selectFirstPropertyType());
        int sizeBeforeDeletion = materialTypeDAO.listEntityTypes().size();
        materialTypeDAO.deleteEntityType(materialType);
        assertEquals(sizeBeforeDeletion - 1, materialTypeDAO.listEntityTypes().size());
    }

    private void assignPropertyType(MaterialTypePE materialType, PropertyTypePE propertyType)
    {
        EntityTypePropertyTypePE assignment =
                createAssignment(EntityKind.MATERIAL, materialType, propertyType);
        daoFactory.getEntityPropertyTypeDAO(EntityKind.MATERIAL)
                .createEntityPropertyTypeAssignment(assignment);
    }

    private List<MaterialPE> createMaterials(int numberOfMaterials, String codePrefix,
            MaterialTypePE materialType)
    {
        ArrayList<MaterialPE> result = new ArrayList<MaterialPE>();
        for (int i = 0; i < numberOfMaterials; i++)
        {
            result.add(createMaterial(materialType, codePrefix + i));
        }
        return result;
    }

    private MaterialTypePE createMaterialType(String code)
    {
        MaterialTypePE entityType = new MaterialTypePE();
        entityType.setCode(code);
        entityType.setDescription("We are living in a material world.");
        entityType.setDatabaseInstance(daoFactory.getHomeDatabaseInstance());
        return entityType;
    }
}