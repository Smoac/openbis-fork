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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.AbstractColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.renderers.SimpleYesNoRenderer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;

/**
 * @author Tomasz Pylak
 */
public enum VocabularyColDefKind implements IColumnDefinitionKind<Vocabulary>
{
    CODE(new AbstractColumnDefinitionKind<Vocabulary>(Dict.CODE, 200)
        {
            @Override
            public String tryGetValue(Vocabulary entity)
            {
                return entity.getCode();
            }
        }),

    DESCRIPTION(new AbstractColumnDefinitionKind<Vocabulary>(Dict.DESCRIPTION, 300)
        {
            @Override
            public String tryGetValue(Vocabulary entity)
            {
                return entity.getDescription();
            }
        }),

    IS_MANAGED_INTERNALLY(new AbstractColumnDefinitionKind<Vocabulary>(Dict.IS_MANAGED_INTERNALLY,
            150)
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
        }),

    URL_TEMPLATE(new AbstractColumnDefinitionKind<Vocabulary>(Dict.URL_TEMPLATE, 300, true)
        {
            @Override
            public String tryGetValue(Vocabulary entity)
            {
                return entity.getURLTemplate();
            }
        }),

    SHOW_IN_CHOOSERS(new AbstractColumnDefinitionKind<Vocabulary>(
            Dict.VOCABULARY_SHOW_AVAILABLE_TERMS_IN_CHOOSERS, true)
        {
            @Override
            public String tryGetValue(Vocabulary entity)
            {
                return SimpleYesNoRenderer.render(entity.isChosenFromList());
            }
        });

    private final AbstractColumnDefinitionKind<Vocabulary> columnDefinitionKind;

    private VocabularyColDefKind(AbstractColumnDefinitionKind<Vocabulary> columnDefinitionKind)
    {
        this.columnDefinitionKind = columnDefinitionKind;
    }

    public String id()
    {
        return name();
    }

    public AbstractColumnDefinitionKind<Vocabulary> getDescriptor()
    {
        return columnDefinitionKind;
    }

}
