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
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.VocabularyTermWithStats;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;

/**
 * @author Tomasz Pylak
 */
public enum VocabularyTermColDefKind implements IColumnDefinitionKind<VocabularyTermWithStats>
{
    CODE(new AbstractColumnDefinitionKind<VocabularyTermWithStats>(Dict.CODE, 200)
        {
            @Override
            public String tryGetValue(VocabularyTermWithStats entity)
            {
                return entity.getTerm().getCode();
            }
        }),

    REGISTRATOR(new AbstractColumnDefinitionKind<VocabularyTermWithStats>(Dict.REGISTRATOR)
        {
            @Override
            public String tryGetValue(VocabularyTermWithStats entity)
            {
                return renderRegistrator(entity.getTerm());
            }
        }),

    REGISTRATION_DATE(new AbstractColumnDefinitionKind<VocabularyTermWithStats>(
            Dict.REGISTRATION_DATE, AbstractColumnDefinitionKind.DATE_COLUMN_WIDTH, false)
        {
            @Override
            public String tryGetValue(VocabularyTermWithStats entity)
            {
                return renderRegistrationDate(entity.getTerm());
            }
        }),

    TOTAL_USAGE(new AbstractColumnDefinitionKind<VocabularyTermWithStats>(Dict.TERM_TOTAL_USAGE)
        {
            @Override
            public String tryGetValue(VocabularyTermWithStats entity)
            {
                return "" + entity.getTotalUsageCounter();
            }
        }),

    SAMPLE_USAGE(new AbstractColumnDefinitionKind<VocabularyTermWithStats>(
            Dict.TERM_FOR_SAMPLES_USAGE, true)
        {
            @Override
            public String tryGetValue(VocabularyTermWithStats entity)
            {
                return "" + entity.getUsageCounter(EntityKind.SAMPLE);
            }
        }),

    EXPERIMENT_USAGE(new AbstractColumnDefinitionKind<VocabularyTermWithStats>(
            Dict.TERM_FOR_EXPERIMENTS_USAGE, true)
        {
            @Override
            public String tryGetValue(VocabularyTermWithStats entity)
            {
                return "" + entity.getUsageCounter(EntityKind.EXPERIMENT);
            }
        }),

    MATERIAL_USAGE(new AbstractColumnDefinitionKind<VocabularyTermWithStats>(
            Dict.TERM_FOR_MATERIALS_USAGE, true)
        {
            @Override
            public String tryGetValue(VocabularyTermWithStats entity)
            {
                return "" + entity.getUsageCounter(EntityKind.MATERIAL);
            }
        }), ;

    private final AbstractColumnDefinitionKind<VocabularyTermWithStats> columnDefinitionKind;

    private VocabularyTermColDefKind(
            AbstractColumnDefinitionKind<VocabularyTermWithStats> columnDefinitionKind)
    {
        this.columnDefinitionKind = columnDefinitionKind;
    }

    public String id()
    {
        return name();
    }

    public AbstractColumnDefinitionKind<VocabularyTermWithStats> getDescriptor()
    {
        return columnDefinitionKind;
    }
}
