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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;

/**
 * @author Tomasz Pylak
 */
public enum VocabularyTermColDefKind implements IColumnDefinitionKind<VocabularyTerm>
{
    CODE(new AbstractColumnDefinitionKind<VocabularyTerm>(Dict.CODE, 200)
        {
            @Override
            public String tryGetValue(VocabularyTerm entity)
            {
                return entity.getCode();
            }
        }),

    REGISTRATOR(new AbstractColumnDefinitionKind<VocabularyTerm>(Dict.REGISTRATOR)
        {
            @Override
            public String tryGetValue(VocabularyTerm entity)
            {
                return renderRegistrator(entity);
            }
        }),

    REGISTRATION_DATE(new AbstractColumnDefinitionKind<VocabularyTerm>(Dict.REGISTRATION_DATE,
            AbstractColumnDefinitionKind.DATE_COLUMN_WIDTH, false)
        {
            @Override
            public String tryGetValue(VocabularyTerm entity)
            {
                return renderRegistrationDate(entity);
            }
        });

    private final AbstractColumnDefinitionKind<VocabularyTerm> columnDefinitionKind;

    private VocabularyTermColDefKind(
            AbstractColumnDefinitionKind<VocabularyTerm> columnDefinitionKind)
    {
        this.columnDefinitionKind = columnDefinitionKind;
    }

    public String id()
    {
        return name();
    }

    public AbstractColumnDefinitionKind<VocabularyTerm> getDescriptor()
    {
        return columnDefinitionKind;
    }
}
