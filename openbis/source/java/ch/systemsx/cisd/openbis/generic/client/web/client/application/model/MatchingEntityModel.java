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

import com.extjs.gxt.ui.client.data.ModelData;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.PersonRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.AbstractColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MatchingEntity;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;

/**
 * A {@link ModelData} implementation for {@link MatchingEntity}.
 * 
 * @author Christian Ribeaud
 */
public final class MatchingEntityModel extends BaseEntityModel<MatchingEntity>
{
    private static final long serialVersionUID = 1L;

    public MatchingEntityModel(final GridRowModel<MatchingEntity> entity)
    {
        super(entity, getStaticColumnsDefinition());

        // override registrator column adding a link
        Person registratorOrNull = entity.getOriginalObject().getRegistrator();
        if (registratorOrNull != null)
        {
            String person = PersonRenderer.createPersonAnchor(registratorOrNull);
            set(MatchingEntityColumnKind.REGISTRATOR.id(), person);
        }
    }

    public static IColumnDefinitionKind<MatchingEntity>[] getStaticColumnsDefinition()
    {
        return MatchingEntityColumnKind.values();
    }

    public enum MatchingEntityColumnKind implements IColumnDefinitionKind<MatchingEntity>
    {
        ENTITY_KIND(new AbstractColumnDefinitionKind<MatchingEntity>(Dict.ENTITY_KIND)
            {
                @Override
                public String tryGetValue(MatchingEntity entity)
                {
                    return entity.getEntityKind().getDescription();
                }
            }),

        ENTITY_TYPE(new AbstractColumnDefinitionKind<MatchingEntity>(Dict.ENTITY_TYPE)
            {
                @Override
                public String tryGetValue(MatchingEntity entity)
                {
                    return entity.getEntityType().getCode();
                }
            }),

        IDENTIFIER(new AbstractColumnDefinitionKind<MatchingEntity>(Dict.IDENTIFIER, 140, false)
            {
                @Override
                public String tryGetValue(MatchingEntity entity)
                {
                    return entity.getIdentifier();
                }
            }),

        REGISTRATOR(new AbstractColumnDefinitionKind<MatchingEntity>(Dict.REGISTRATOR)
            {
                @Override
                public String tryGetValue(MatchingEntity entity)
                {
                    return renderRegistratorPerson(entity.getRegistrator());
                }
            }),

        MATCHING_FIELD(new AbstractColumnDefinitionKind<MatchingEntity>(Dict.MATCHING_FIELD, 140,
                false)
            {
                @Override
                public String tryGetValue(MatchingEntity entity)
                {
                    return entity.getFieldDescription();
                }
            }),

        MATCHING_TEXT(new AbstractColumnDefinitionKind<MatchingEntity>(Dict.MATCHING_TEXT, 200,
                false)
            {
                @Override
                public String tryGetValue(MatchingEntity entity)
                {
                    return entity.getTextFragment();
                }
            });

        private final AbstractColumnDefinitionKind<MatchingEntity> columnDefinitionKind;

        private MatchingEntityColumnKind(
                AbstractColumnDefinitionKind<MatchingEntity> columnDefinitionKind)
        {
            this.columnDefinitionKind = columnDefinitionKind;
        }

        public String id()
        {
            return name();
        }

        public AbstractColumnDefinitionKind<MatchingEntity> getDescriptor()
        {
            return columnDefinitionKind;
        }
    }

}
