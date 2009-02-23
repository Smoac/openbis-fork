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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import java.util.ArrayList;

import org.jmock.Expectations;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool;
import ch.systemsx.cisd.openbis.generic.shared.CommonTestUtils;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * Test cases for corresponding {@link MaterialTable} class.
 * 
 * @author Izabela Adamczyk
 */
public final class MaterialTableTest extends AbstractBOTest
{
    private final MaterialTable createMaterialTable()
    {
        return new MaterialTable(daoFactory, ManagerTestTool.EXAMPLE_SESSION);
    }

    @Test
    public void testLoad() throws Exception
    {
        final MaterialTypePE materialType = CommonTestUtils.createMaterialType();
        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getEntityTypeDAO(EntityKind.MATERIAL);
                    will(returnValue(entityTypeDAO));

                    allowing(daoFactory).getMaterialDAO();
                    will(returnValue(materialDAO));

                    one(entityTypeDAO).tryToFindEntityTypeByCode(materialType.getCode());
                    will(returnValue(materialType));

                    one(materialDAO).listMaterials(materialType);
                    will(returnValue(new ArrayList<MaterialPE>()));
                }
            });
        createMaterialTable().load(materialType.getCode());
        context.assertIsSatisfied();
    }
}
