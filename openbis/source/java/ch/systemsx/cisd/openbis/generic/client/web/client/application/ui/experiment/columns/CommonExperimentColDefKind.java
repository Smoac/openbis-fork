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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
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
    CODE(new AbstractColumnDefinitionKind<Experiment>(ModelDataPropertyNames.CODE, Dict.CODE)
        {
            public String tryGetValue(Experiment entity)
            {
                return entity.getCode();
            }
        }),

    EXPERIMENT_TYPE(new AbstractColumnDefinitionKind<Experiment>(
            ModelDataPropertyNames.EXPERIMENT_TYPE_CODE_FOR_EXPERIMENT, Dict.EXPERIMENT_TYPE)
        {
            public String tryGetValue(Experiment entity)
            {
                return entity.getExperimentType().getCode();
            }
        }),

    EXPERIMENT_IDENTIFIER(new AbstractColumnDefinitionKind<Experiment>(
            ModelDataPropertyNames.EXPERIMENT_IDENTIFIER, Dict.EXPERIMENT_IDENTIFIER, 150, true)
        {
            public String tryGetValue(Experiment entity)
            {
                return entity.getIdentifier();
            }
        }),

    GROUP(new AbstractColumnDefinitionKind<Experiment>(ModelDataPropertyNames.GROUP_FOR_EXPERIMENT,
            Dict.GROUP)
        {
            public String tryGetValue(Experiment entity)
            {
                return entity.getProject().getGroup().getCode();
            }
        }),

    PROJECT(new AbstractColumnDefinitionKind<Experiment>(ModelDataPropertyNames.PROJECT,
            Dict.PROJECT)
        {
            public String tryGetValue(Experiment entity)
            {
                return entity.getProject().getCode();
            }
        }),

    REGISTRATOR(new AbstractColumnDefinitionKind<Experiment>(ModelDataPropertyNames.REGISTRATOR,
            Dict.REGISTRATOR)
        {
            public String tryGetValue(Experiment entity)
            {
                return renderRegistrator(entity);
            }
        }),

    REGISTRATION_DATE(new AbstractColumnDefinitionKind<Experiment>(
            ModelDataPropertyNames.REGISTRATION_DATE, Dict.REGISTRATION_DATE, 200, false)
        {
            public String tryGetValue(Experiment entity)
            {
                return renderRegistrationDate(entity);
            }
        }),

    IS_INVALID(new AbstractColumnDefinitionKind<Experiment>(ModelDataPropertyNames.IS_INVALID,
            Dict.IS_INVALID, true)
        {
            public String tryGetValue(Experiment entity)
            {
                return renderInvalidationFlag(entity);
            }
        });

    private final IColumnDefinitionKind<Experiment> columnDefinitionKind;

    private CommonExperimentColDefKind(IColumnDefinitionKind<Experiment> columnDefinitionKind)
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
        return columnDefinitionKind.id();
    }

    public boolean isHidden()
    {
        return columnDefinitionKind.isHidden();
    }

    public String tryGetValue(Experiment entity)
    {
        return columnDefinitionKind.tryGetValue(entity);
    }

}