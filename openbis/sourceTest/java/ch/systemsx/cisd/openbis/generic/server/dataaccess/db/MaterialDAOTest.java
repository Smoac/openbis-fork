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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.Assert;

import org.springframework.dao.DataIntegrityViolationException;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * Test cases for corresponding {@link MaterialDAO} class.
 * 
 * @author Izabela Adamczyk
 */
@Test(groups =
    { "db", "material" })
public final class MaterialDAOTest extends AbstractDAOTest
{
    private static final String BACTERIUM = "BACTERIUM";

    private static final String BRAND_NEW_BACTERIUM = "BRAND_NEW_BACTERIUM";

    final int NUMBER_OF_BACTERIA = 4;

    @Test
    public void testListMaterials() throws Exception
    {
        MaterialTypePE type =
                (MaterialTypePE) daoFactory.getEntityTypeDAO(EntityKind.MATERIAL)
                        .tryToFindEntityTypeByCode(BACTERIUM);
        List<MaterialPE> list =
                daoFactory.getMaterialDAO().listMaterialsWithPropertiesAndInhibitor(type);
        Assert.assertEquals(NUMBER_OF_BACTERIA, list.size());
        Collections.sort(list);
        Assert.assertEquals(list.get(0).getCode(), "BACTERIUM-X");
    }

    @Test
    public void testCreateMaterials() throws Exception
    {
        MaterialTypePE type =
                (MaterialTypePE) daoFactory.getEntityTypeDAO(EntityKind.MATERIAL)
                        .tryToFindEntityTypeByCode(BACTERIUM);
        List<MaterialPE> bacteria_before =
                daoFactory.getMaterialDAO().listMaterialsWithPropertiesAndInhibitor(type);
        Assert.assertEquals(NUMBER_OF_BACTERIA, bacteria_before.size());
        List<MaterialPE> newMaterials = new ArrayList<MaterialPE>();
        newMaterials.add(createMaterial(type, "BRAND_NEW_BACTERIUM_1"));
        newMaterials.add(createMaterial(type, "BRAND_NEW_BACTERIUM_2"));
        Collections.sort(newMaterials);
        daoFactory.getMaterialDAO().createMaterials(newMaterials);
        List<MaterialPE> bacteria_after =
                daoFactory.getMaterialDAO().listMaterialsWithPropertiesAndInhibitor(type);
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
                daoFactory.getMaterialDAO().listMaterialsWithPropertiesAndInhibitor(type);
        String existingBacteriumCode = bacteria_before.get(0).getCode();
        List<MaterialPE> newMaterials = new ArrayList<MaterialPE>();
        newMaterials.add(createMaterial(type, existingBacteriumCode));
        daoFactory.getMaterialDAO().createMaterials(newMaterials);
    }

}