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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.columns;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Experiment;

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
        }),

    DATABASE_INSTANCE(new AbstractColumnDefinitionKind<Experiment>(Dict.DATABASE_INSTANCE, true)
        {
            @Override
            public String tryGetValue(Experiment entity)
            {
                return entity.getProject().getGroup().getInstance().getCode();
            }
        }),

    GROUP(new AbstractColumnDefinitionKind<Experiment>(Dict.GROUP, true)
        {
            @Override
            public String tryGetValue(Experiment entity)
            {
                return entity.getProject().getGroup().getCode();
            }
        }),

    PROJECT(new AbstractColumnDefinitionKind<Experiment>(Dict.PROJECT, true)
        {
            @Override
            public String tryGetValue(Experiment entity)
            {
                return entity.getProject().getCode();
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