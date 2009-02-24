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

import java.util.Collections;
import java.util.List;

import junit.framework.Assert;

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

    @Test
    public void testListMaterials() throws Exception
    {
        MaterialTypePE type =
                (MaterialTypePE) daoFactory.getEntityTypeDAO(EntityKind.MATERIAL)
                        .tryToFindEntityTypeByCode("BACTERIUM");
        List<MaterialPE> list = daoFactory.getMaterialDAO().listMaterialsWithPropertiesAndInhibitor(type);
        Assert.assertEquals(4, list.size());
        Collections.sort(list);
        Assert.assertEquals(list.get(0).getCode(), "BACTERIUM-X");
    }

}