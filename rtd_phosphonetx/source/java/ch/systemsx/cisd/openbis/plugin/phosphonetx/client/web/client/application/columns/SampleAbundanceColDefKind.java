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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.application.columns;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.AbstractColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.SampleWithPropertiesAndAbundance;

public enum SampleAbundanceColDefKind implements
        IColumnDefinitionKind<SampleWithPropertiesAndAbundance>
{
    CODE(new AbstractColumnDefinitionKind<SampleWithPropertiesAndAbundance>(Dict.CODE)
        {
            @Override
            public String tryGetValue(SampleWithPropertiesAndAbundance entity)
            {
                return entity.getCode();
            }
        }),

    SAMPLE_IDENTIFIER(new AbstractColumnDefinitionKind<SampleWithPropertiesAndAbundance>(
            Dict.SAMPLE_IDENTIFIER)
        {
            @Override
            public String tryGetValue(SampleWithPropertiesAndAbundance entity)
            {
                return entity.getIdentifier();
            }
        }),

    SAMPLE_TYPE(new AbstractColumnDefinitionKind<SampleWithPropertiesAndAbundance>(
            Dict.SAMPLE_TYPE, 150)
        {
            @Override
            public String tryGetValue(SampleWithPropertiesAndAbundance entity)
            {
                return entity.getSampleType().getCode();
            }
        }),

    ABUNDANCE(
            new AbstractColumnDefinitionKind<SampleWithPropertiesAndAbundance>(
                    ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.application.Dict.ABUNDANCE,
                    100, false, true)
                {
                    @Override
                    public String tryGetValue(SampleWithPropertiesAndAbundance entity)
                    {
                        return Double.toString(entity.getAbundance());
                    }
                });

    private final AbstractColumnDefinitionKind<SampleWithPropertiesAndAbundance> columnDefinitionKind;

    private SampleAbundanceColDefKind(
            AbstractColumnDefinitionKind<SampleWithPropertiesAndAbundance> columnDefinitionKind)
    {
        this.columnDefinitionKind = columnDefinitionKind;
    }

    public String id()
    {
        return name();
    }

    public AbstractColumnDefinitionKind<SampleWithPropertiesAndAbundance> getDescriptor()
    {
        return columnDefinitionKind;
    }

}