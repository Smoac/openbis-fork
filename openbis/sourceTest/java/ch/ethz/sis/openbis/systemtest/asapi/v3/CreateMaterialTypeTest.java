/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

package ch.ethz.sis.openbis.systemtest.asapi.v3;

import java.util.List;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.MaterialType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.create.MaterialTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.fetchoptions.MaterialTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.search.MaterialTypeSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;

/**
 * @author pkupczyk
 */
@Test
public class CreateMaterialTypeTest extends CreateEntityTypeTest<MaterialTypeCreation, MaterialType>
{

    @Override
    protected EntityKind getEntityKind()
    {
        return EntityKind.MATERIAL;
    }

    @Override
    protected MaterialTypeCreation newTypeCreation()
    {
        return new MaterialTypeCreation();
    }

    @Override
    protected void fillTypeSpecificFields(MaterialTypeCreation creation)
    {
        // nothing to do
    }

    @Override
    protected void createTypes(String sessionToken, List<MaterialTypeCreation> creations)
    {
        v3api.createMaterialTypes(sessionToken, creations);
    }

    @Override
    protected MaterialType getType(String sessionToken, String code)
    {
        MaterialTypeSearchCriteria criteria = new MaterialTypeSearchCriteria();
        criteria.withId().thatEquals(new EntityTypePermId(code));

        MaterialTypeFetchOptions fo = new MaterialTypeFetchOptions();
        fo.withPropertyAssignments().withPropertyType();
        fo.withPropertyAssignments().withRegistrator();

        SearchResult<MaterialType> result = v3api.searchMaterialTypes(sessionToken, criteria, fo);

        if (result.getObjects().size() > 0)
        {
            return result.getObjects().get(0);
        } else
        {
            return null;
        }
    }

    @Override
    protected void assertTypeSpecificFields(MaterialTypeCreation creation, MaterialType type)
    {
        // nothing to do
    }

}
