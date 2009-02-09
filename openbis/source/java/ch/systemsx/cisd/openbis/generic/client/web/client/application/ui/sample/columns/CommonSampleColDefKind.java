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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.columns;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.renderer.SimpleYesNoRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Group;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Procedure;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;

public enum CommonSampleColDefKind implements IColumnDefinitionKind<Sample>
{
    DATABASE_INSTANCE(new AbstractColumnDefinitionKind<Sample>(Dict.DATABASE_INSTANCE, true)
        {
            @Override
            public String tryGetValue(Sample entity)
            {
                DatabaseInstance databaseInstance = entity.getDatabaseInstance();
                if (databaseInstance == null)
                {
                    databaseInstance = entity.getGroup().getInstance();
                }
                return databaseInstance.getCode();
            }
        }),

    GROUP(new AbstractColumnDefinitionKind<Sample>(Dict.GROUP, true)
        {
            @Override
            public String tryGetValue(Sample entity)
            {
                final Group group = entity.getGroup();
                return group == null ? "" : group.getCode();
            }
        }),

    CODE(new AbstractColumnDefinitionKind<Sample>(Dict.CODE)
        {
            @Override
            public String tryGetValue(Sample entity)
            {
                return entity.getCode();
            }
        }),

    SAMPLE_IDENTIFIER(new AbstractColumnDefinitionKind<Sample>(Dict.SAMPLE_IDENTIFIER, 150, true)
        {
            @Override
            public String tryGetValue(Sample entity)
            {
                return entity.getIdentifier();
            }
        }),

    IS_INSTANCE_SAMPLE(new AbstractColumnDefinitionKind<Sample>(Dict.IS_INSTANCE_SAMPLE, true)
        {
            @Override
            public String tryGetValue(Sample entity)
            {
                return SimpleYesNoRenderer.render(entity.getDatabaseInstance() != null);
            }
        }),

    IS_INVALID(new AbstractColumnDefinitionKind<Sample>(Dict.IS_INVALID, true)
        {
            @Override
            public String tryGetValue(Sample entity)
            {
                return renderInvalidationFlag(entity);
            }
        }),

    EXPERIMENT(new AbstractColumnDefinitionKind<Sample>(Dict.EXPERIMENT)
        {
            @Override
            public String tryGetValue(Sample entity)
            {
                final Experiment exp = tryToGetExperiment(entity);
                return exp == null ? null : exp.getCode();
            }
        }),

    EXPERIMENT_IDENTIFIER(new AbstractColumnDefinitionKind<Sample>(Dict.EXPERIMENT_IDENTIFIER, 200,
            true)
        {
            @Override
            public String tryGetValue(Sample entity)
            {
                final Experiment exp = tryToGetExperiment(entity);
                return exp == null ? null : exp.getIdentifier();
            }
        }),

    PROJECT(new AbstractColumnDefinitionKind<Sample>(Dict.PROJECT)
        {
            @Override
            public String tryGetValue(Sample entity)
            {
                final Experiment exp = tryToGetExperiment(entity);
                return exp == null ? null : exp.getProject().getCode();
            }
        }),

    REGISTRATOR(new AbstractColumnDefinitionKind<Sample>(Dict.REGISTRATOR)
        {
            @Override
            public String tryGetValue(Sample entity)
            {
                return renderRegistrator(entity);
            }
        }),

    REGISTRATION_DATE(new AbstractColumnDefinitionKind<Sample>(Dict.REGISTRATION_DATE,
            AbstractColumnDefinitionKind.DATE_COLUMN_WIDTH)
        {
            @Override
            public String tryGetValue(Sample entity)
            {
                return renderRegistrationDate(entity);
            }
        });

    private final AbstractColumnDefinitionKind<Sample> columnDefinitionKind;

    private CommonSampleColDefKind(AbstractColumnDefinitionKind<Sample> columnDefinitionKind)
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

    public String tryGetValue(Sample entity)
    {
        return columnDefinitionKind.tryGetValue(entity);
    }

    private final static Experiment tryToGetExperiment(final Sample sample)
    {
        final Procedure procedure = sample.getValidProcedure();
        if (procedure != null)
        {
            return procedure.getExperiment();
        }
        return null;
    }

}