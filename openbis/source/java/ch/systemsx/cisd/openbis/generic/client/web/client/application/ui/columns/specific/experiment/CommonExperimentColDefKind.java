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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.experiment;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.AbstractColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.LinkExtractor;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;

/**
 * Definition of experiment table columns.
 * 
 * @author Tomasz Pylak
 */
public enum CommonExperimentColDefKind implements IColumnDefinitionKind<Experiment>
{
    CODE(new AbstractColumnDefinitionKind<Experiment>(Dict.CODE)
        {
            @Override
            public String tryGetValue(Experiment entity)
            {
                return entity.getCode();
            }

            @Override
            public String tryGetLink(Experiment entity)
            {
                return LinkExtractor.tryExtract(entity);
            }
        }),

    EXPERIMENT_TYPE(new AbstractColumnDefinitionKind<Experiment>(Dict.EXPERIMENT_TYPE, true)
        {
            @Override
            public String tryGetValue(Experiment entity)
            {
                return entity.getExperimentType().getCode();
            }
        }),

    EXPERIMENT_IDENTIFIER(new AbstractColumnDefinitionKind<Experiment>(Dict.EXPERIMENT_IDENTIFIER,
            150, true)
        {
            @Override
            public String tryGetValue(Experiment entity)
            {
                return entity.getIdentifier();
            }

            @Override
            public String tryGetLink(Experiment entity)
            {
                return LinkExtractor.tryExtract(entity);
            }
        }),

    DATABASE_INSTANCE(new AbstractColumnDefinitionKind<Experiment>(Dict.DATABASE_INSTANCE, true)
        {
            @Override
            public String tryGetValue(Experiment entity)
            {
                return entity.getProject().getSpace().getInstance().getCode();
            }
        }),

    GROUP(new AbstractColumnDefinitionKind<Experiment>(Dict.GROUP, true)
        {
            @Override
            public String tryGetValue(Experiment entity)
            {
                return entity.getProject().getSpace().getCode();
            }
        }),

    PROJECT(new AbstractColumnDefinitionKind<Experiment>(Dict.PROJECT, true)
        {
            @Override
            public String tryGetValue(Experiment entity)
            {
                return entity.getProject().getCode();
            }

            @Override
            public String tryGetLink(Experiment entity)
            {
                return LinkExtractor.tryExtract(entity.getProject());
            }
        }),

    REGISTRATOR(new AbstractColumnDefinitionKind<Experiment>(Dict.REGISTRATOR)
        {
            @Override
            public String tryGetValue(Experiment entity)
            {
                return renderRegistrator(entity);
            }
        }),

    REGISTRATION_DATE(new AbstractColumnDefinitionKind<Experiment>(Dict.REGISTRATION_DATE, 200,
            false)
        {
            @Override
            public String tryGetValue(Experiment entity)
            {
                return renderRegistrationDate(entity);
            }
        }),

    IS_INVALID(new AbstractColumnDefinitionKind<Experiment>(Dict.IS_INVALID, true)
        {
            @Override
            public String tryGetValue(Experiment entity)
            {
                return renderInvalidationFlag(entity);
            }
        }),

    PERM_ID(new AbstractColumnDefinitionKind<Experiment>(Dict.PERM_ID, true)
        {
            @Override
            public String tryGetValue(Experiment entity)
            {
                return entity.getPermId();
            }
        }),

    SHOW_DETAILS_LINK(new AbstractColumnDefinitionKind<Experiment>(Dict.SHOW_DETAILS_LINK, true)
        {
            @Override
            public String tryGetValue(Experiment entity)
            {
                return entity.getPermlink();
            }
        });

    private final AbstractColumnDefinitionKind<Experiment> columnDefinitionKind;

    private CommonExperimentColDefKind(AbstractColumnDefinitionKind<Experiment> columnDefinitionKind)
    {
        this.columnDefinitionKind = columnDefinitionKind;
    }

    public String id()
    {
        return name();
    }

    public AbstractColumnDefinitionKind<Experiment> getDescriptor()
    {
        return columnDefinitionKind;
    }
}
