/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.sample;

import static ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.EntityTypeColDefKindFactory.sampleTypeColDefKindFactory;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.AbstractColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.renderers.SimpleYesNoRenderer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;

/**
 * Columns definition for browsing grid of {@link SampleType}s.
 * 
 * @author Piotr Buczek
 */
public enum SampleTypeColDefKind implements IColumnDefinitionKind<SampleType>
{
    // copy from EntityTypeColDefKind (cannot extend an enum)

    CODE(sampleTypeColDefKindFactory.createCodeColDefKind()),

    DESCRIPTION(sampleTypeColDefKindFactory.createDescriptionColDefKind()),

    DATABASE_INSTANCE(sampleTypeColDefKindFactory.createDatabaseInstanceColDefKind()),

    // specific Sample Type columns

    IS_LISTABLE(new AbstractColumnDefinitionKind<SampleType>(Dict.IS_LISTABLE, true)
        {
            @Override
            public String tryGetValue(SampleType entity)
            {
                return SimpleYesNoRenderer.render(entity.isListable());
            }
        }),

    IS_SHOW_CONTAINER(new AbstractColumnDefinitionKind<SampleType>(Dict.IS_SHOW_CONTAINER, 200,
            true)
        {
            @Override
            public String tryGetValue(SampleType entity)
            {
                return SimpleYesNoRenderer.render(entity.isShowContainer());
            }
        }),

    GENERATED_FROM_HIERARCHY_DEPTH(new AbstractColumnDefinitionKind<SampleType>(
            Dict.GENERATED_FROM_HIERARCHY_DEPTH, 200, true)
        {
            @Override
            public String tryGetValue(SampleType entity)
            {
                return Integer.toString(entity.getGeneratedFromHierarchyDepth());
            }
        });

    private final AbstractColumnDefinitionKind<SampleType> columnDefinitionKind;

    private SampleTypeColDefKind(AbstractColumnDefinitionKind<SampleType> columnDefinitionKind)
    {
        this.columnDefinitionKind = columnDefinitionKind;
    }

    public String id()
    {
        return name();
    }

    public AbstractColumnDefinitionKind<SampleType> getDescriptor()
    {
        return columnDefinitionKind;
    }

}
