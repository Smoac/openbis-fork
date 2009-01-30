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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.vocabulary;

import ch.systemsx.cisd.openbis.generic.client.shared.Vocabulary;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.renderer.SimpleYesNoRenderer;

/**
 * @author Tomasz Pylak
 */
public enum VocabularyColDefKind implements IColumnDefinitionKind<Vocabulary>
{
    CODE(new AbstractColumnDefinitionKind<Vocabulary>(Dict.CODE)
        {
            @Override
            public String tryGetValue(Vocabulary entity)
            {
                return entity.getCode();
            }
        }),

    DESCRIPTION(new AbstractColumnDefinitionKind<Vocabulary>(Dict.DESCRIPTION)
        {
            @Override
            public String tryGetValue(Vocabulary entity)
            {
                return entity.getDescription();
            }
        }),

    IS_MANAGED_INTERNALLY(new AbstractColumnDefinitionKind<Vocabulary>(Dict.IS_MANAGED_INTERNALLY)
        {
            @Override
            public String tryGetValue(Vocabulary entity)
            {
                return SimpleYesNoRenderer.render(entity.isManagedInternally());
            }
        }),

    REGISTRATOR(new AbstractColumnDefinitionKind<Vocabulary>(Dict.REGISTRATOR)
        {
            @Override
            public String tryGetValue(Vocabulary entity)
            {
                return renderRegistrator(entity);
            }
        }),

    REGISTRATION_DATE(new AbstractColumnDefinitionKind<Vocabulary>(Dict.REGISTRATION_DATE,
            AbstractColumnDefinitionKind.DATE_COLUMN_WIDTH, false)
        {
            @Override
            public String tryGetValue(Vocabulary entity)
            {
                return renderRegistrationDate(entity);
            }
        });

    private final AbstractColumnDefinitionKind<Vocabulary> columnDefinitionKind;

    private VocabularyColDefKind(AbstractColumnDefinitionKind<Vocabulary> columnDefinitionKind)
    {
        this.columnDefinitionKind = columnDefinitionKind;
    }

    public String getHeaderMsgKey()
    {
        return columnDefinitionKind.getHeaderMsgKey();
    }

    public int getWidth()
    {
        return columnDefinitionKind.getWidth();
    }

    public String id()
    {
        return name();
    }

    public boolean isHidden()
    {
        return columnDefinitionKind.isHidden();
    }

    public String tryGetValue(Vocabulary entity)
    {
        return columnDefinitionKind.tryGetValue(entity);
    }
}
