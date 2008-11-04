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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.model;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.ModelData;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.PersonRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.MatchingEntity;

/**
 * A {@link ModelData} implementation for {@link MatchingEntity}.
 * 
 * @author Christian Ribeaud
 */
public final class MatchingEntityModel extends BaseModelData
{
    private static final long serialVersionUID = 1L;

    public MatchingEntityModel()
    {
    }

    public MatchingEntityModel(final MatchingEntity matchingEntity)
    {
        set(ModelDataPropertyNames.OBJECT, matchingEntity);
        set(ModelDataPropertyNames.IDENTIFIER, matchingEntity.getIdentifier());
        set(ModelDataPropertyNames.ENTITY_KIND, matchingEntity.getEntityKind().getDescription());
        set(ModelDataPropertyNames.ENTITY_TYPE, matchingEntity.getEntityType().getCode());
        set(ModelDataPropertyNames.REGISTRATOR, PersonRenderer.createPersonAnchor(matchingEntity
                .getRegistrator()));
    }

    public final static List<MatchingEntityModel> convert(
            final List<MatchingEntity> matchingEntities)
    {
        final List<MatchingEntityModel> list = new ArrayList<MatchingEntityModel>();
        for (final MatchingEntity matchingEntity : matchingEntities)
        {
            list.add(new MatchingEntityModel(matchingEntity));
        }
        return list;
    }
}
