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

import ch.systemsx.cisd.openbis.generic.client.shared.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.renderer.SimpleYesNoRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Group;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Procedure;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;

public enum CommonSampleColDefKind implements IColumnDefinitionKind<Sample>
{
    DATABASE_INSTANCE(new AbstractColumnDefinitionKind<Sample>(
            ModelDataPropertyNames.DATABASE_INSTANCE, Dict.DATABASE_INSTANCE, true)
        {
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

    GROUP(new AbstractColumnDefinitionKind<Sample>(ModelDataPropertyNames.GROUP, Dict.GROUP)
        {
            public String tryGetValue(Sample entity)
            {
                final Group group = entity.getGroup();
                return group == null ? "" : group.getCode();
            }
        }),

    CODE(new AbstractColumnDefinitionKind<Sample>(ModelDataPropertyNames.CODE, Dict.CODE)
        {
            public String tryGetValue(Sample entity)
            {
                return entity.getCode();
            }
        }),

    SAMPLE_IDENTIFIER(new AbstractColumnDefinitionKind<Sample>(
            ModelDataPropertyNames.SAMPLE_IDENTIFIER, Dict.SAMPLE_IDENTIFIER, 150, true)
        {
            public String tryGetValue(Sample entity)
            {
                return entity.getIdentifier();
            }
        }),

    IS_INSTANCE_SAMPLE(new AbstractColumnDefinitionKind<Sample>(
            ModelDataPropertyNames.IS_INSTANCE_SAMPLE, Dict.IS_INSTANCE_SAMPLE, true)
        {
            public String tryGetValue(Sample entity)
            {
                return SimpleYesNoRenderer.render(entity.getDatabaseInstance() != null);
            }
        }),

    REGISTRATOR(new AbstractColumnDefinitionKind<Sample>(ModelDataPropertyNames.REGISTRATOR,
            Dict.REGISTRATOR, true)
        {
            public String tryGetValue(Sample entity)
            {
                return renderRegistrator(entity);
            }
        }),

    REGISTRATION_DATE(new AbstractColumnDefinitionKind<Sample>(
            ModelDataPropertyNames.REGISTRATION_DATE, Dict.REGISTRATION_DATE, 200, true)
        {
            public String tryGetValue(Sample entity)
            {
                return renderRegistrationDate(entity);
            }
        }),

    IS_INVALID(new AbstractColumnDefinitionKind<Sample>(ModelDataPropertyNames.IS_INVALID,
            Dict.IS_INVALID, true)
        {
            public String tryGetValue(Sample entity)
            {
                return renderInvalidationFlag(entity);
            }
        }),

    PROJECT_FOR_SAMPLE(new AbstractColumnDefinitionKind<Sample>(
            ModelDataPropertyNames.PROJECT_FOR_SAMPLE, Dict.PROJECT)
        {
            public String tryGetValue(Sample entity)
            {
                final Experiment exp = tryToGetExperiment(entity);
                return exp == null ? null : exp.getProject().getCode();
            }
        }),

    EXPERIMENT_FOR_SAMPLE(new AbstractColumnDefinitionKind<Sample>(
            ModelDataPropertyNames.EXPERIMENT_FOR_SAMPLE, Dict.EXPERIMENT)
        {
            public String tryGetValue(Sample entity)
            {
                final Experiment exp = tryToGetExperiment(entity);
                return exp == null ? null : exp.getCode();
            }
        }),

    EXPERIMENT_IDENTIFIER_FOR_SAMPLE(new AbstractColumnDefinitionKind<Sample>(
            ModelDataPropertyNames.EXPERIMENT_IDENTIFIER_FOR_SAMPLE, Dict.EXPERIMENT_IDENTIFIER,
            200, true)
        {
            public String tryGetValue(Sample entity)
            {
                final Experiment exp = tryToGetExperiment(entity);
                return exp == null ? null : exp.getIdentifier();
            }
        });

    private final IColumnDefinitionKind<Sample> columnDefinitionKind;

    private CommonSampleColDefKind(IColumnDefinitionKind<Sample> columnDefinitionKind)
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