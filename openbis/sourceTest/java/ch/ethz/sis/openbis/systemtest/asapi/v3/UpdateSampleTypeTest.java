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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.SampleTypeUpdate;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Script;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Test
public class UpdateSampleTypeTest extends UpdateEntityTypeTest<SampleTypeUpdate, SampleType>
{

    @Override
    protected EntityKind getEntityKind()
    {
        return EntityKind.SAMPLE;
    }

    @Override
    protected SampleTypeUpdate newTypeUpdate()
    {
        return new SampleTypeUpdate();
    }

    @Override
    protected EntityTypePermId getTypeId()
    {
        return new EntityTypePermId("MASTER_PLATE", ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind.SAMPLE);
    }

    @Override
    protected void updateTypes(String sessionToken, List<SampleTypeUpdate> updates)
    {
        v3api.updateSampleTypes(sessionToken, updates);
    }

    @Override
    protected SampleType getType(String sessionToken, EntityTypePermId typeId)
    {
        SampleTypeSearchCriteria searchCriteria = new SampleTypeSearchCriteria();
        searchCriteria.withPermId().thatEquals(typeId.getPermId());
        SampleTypeFetchOptions fetchOptions = new SampleTypeFetchOptions();
        fetchOptions.withPropertyAssignments().withEntityType();
        fetchOptions.withPropertyAssignments().withPropertyType();
        return v3api.searchSampleTypes(sessionToken, searchCriteria, fetchOptions).getObjects().get(0);
    }

    @Override
    protected String getValidationPluginOrNull(String sessionToken, EntityTypePermId typeId)
    {
        for (ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType sampleType : commonServer.listSampleTypes(sessionToken))
        {
            if (sampleType.getCode().equals(typeId.getPermId()))
            {
                Script validationScript = sampleType.getValidationScript();
                return validationScript == null ? null : validationScript.getName();
            }
        }
        return null;
    }

    @Override
    protected AbstractEntitySearchCriteria<?> createSearchCriteria(EntityTypePermId typeId)
    {
        SampleSearchCriteria sampleSearchCriteria = new SampleSearchCriteria();
        sampleSearchCriteria.withType().withId().thatEquals(typeId);
        return sampleSearchCriteria;
    }

    @Override
    protected List<? extends IPropertiesHolder> searchEntities(String sessionToken, AbstractEntitySearchCriteria<?> searchCriteria)
    {
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProperties();
        return v3api.searchSamples(sessionToken, (SampleSearchCriteria) searchCriteria, fetchOptions).getObjects();
    }

}
