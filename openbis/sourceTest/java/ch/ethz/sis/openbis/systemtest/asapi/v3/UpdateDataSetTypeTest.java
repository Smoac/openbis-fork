/*
 * Copyright 2017 ETH Zuerich, SIS
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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPropertiesHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractEntitySearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.DataSetTypeUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Script;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Test
public class UpdateDataSetTypeTest extends UpdateEntityTypeTest<DataSetTypeUpdate, DataSetType>
{

    @Override
    protected EntityKind getEntityKind()
    {
        return EntityKind.DATA_SET;
    }

    @Override
    protected DataSetTypeUpdate newTypeUpdate()
    {
        return new DataSetTypeUpdate();
    }

    @Override
    protected EntityTypePermId getTypeId()
    {
        return new EntityTypePermId("DELETION_TEST", ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind.DATA_SET);
    }

    @Override
    protected void updateTypes(String sessionToken, List<DataSetTypeUpdate> updates)
    {
        v3api.updateDataSetTypes(sessionToken, updates);
    }

    @Override
    protected DataSetType getType(String sessionToken, EntityTypePermId typeId)
    {
        DataSetTypeSearchCriteria searchCriteria = new DataSetTypeSearchCriteria();
        searchCriteria.withPermId().thatEquals(typeId.getPermId());
        DataSetTypeFetchOptions fetchOptions = new DataSetTypeFetchOptions();
        fetchOptions.withPropertyAssignments().withEntityType();
        fetchOptions.withPropertyAssignments().withPropertyType();
        return v3api.searchDataSetTypes(sessionToken, searchCriteria, fetchOptions).getObjects().get(0);
    }

    @Override
    protected String getValidationPluginOrNull(String sessionToken, EntityTypePermId typeId)
    {
        for (ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType entityType : commonServer.listDataSetTypes(sessionToken))
        {
            if (entityType.getCode().equals(typeId.getPermId()))
            {
                Script validationScript = entityType.getValidationScript();
                return validationScript == null ? null : validationScript.getName();
            }
        }
        return null;
    }

    @Override
    protected AbstractEntitySearchCriteria<?> createSearchCriteria(EntityTypePermId typeId)
    {
        DataSetSearchCriteria sarchCriteria = new DataSetSearchCriteria();
        sarchCriteria.withType().withId().thatEquals(typeId);
        return sarchCriteria;
    }

    @Override
    protected List<? extends IPropertiesHolder> searchEntities(String sessionToken, AbstractEntitySearchCriteria<?> searchCriteria)
    {
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withProperties();
        return v3api.searchDataSets(sessionToken, (DataSetSearchCriteria) searchCriteria, fetchOptions).getObjects();
    }
}
