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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.data;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.AbstractColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalData;

/**
 * Definition of data set search results table columns.
 * 
 * @author Izabela Adamczyk
 */
public enum DataSetSearchHitColDefKind implements IColumnDefinitionKind<ExternalData>
{

    CODE(new AbstractColumnDefinitionKind<ExternalData>(Dict.CODE)
        {
            @Override
            public String tryGetValue(ExternalData entity)
            {
                return entity.getCode();
            }
        }),

    PARENT(new AbstractColumnDefinitionKind<ExternalData>(Dict.PARENT, true)
        {
            @Override
            public String tryGetValue(ExternalData entity)
            {
                return entity.getParentCode();
            }
        }),

    LOCATION(new AbstractColumnDefinitionKind<ExternalData>(Dict.LOCATION, 200, true)
        {
            @Override
            public String tryGetValue(ExternalData entity)
            {
                return entity.getLocation();
            }
        }),

    DATA_SET_TYPE(new AbstractColumnDefinitionKind<ExternalData>(Dict.DATA_SET_TYPE, 120)
        {
            @Override
            public String tryGetValue(ExternalData entity)
            {
                return entity.getDataSetType().getCode();
            }
        }),

    FILE_TYPE(new AbstractColumnDefinitionKind<ExternalData>(Dict.FILE_FORMAT_TYPE, 120)
        {
            @Override
            public String tryGetValue(ExternalData entity)
            {
                return entity.getFileFormatType().getCode();
            }
        }),

    SAMPLE(new AbstractColumnDefinitionKind<ExternalData>(Dict.SAMPLE, 100)
        {
            @Override
            public String tryGetValue(ExternalData entity)
            {
                return entity.getSampleCode();
            }
        }),

    SAMPLE_IDENTIFIER(new AbstractColumnDefinitionKind<ExternalData>(Dict.SAMPLE_IDENTIFIER, true)
        {
            @Override
            public String tryGetValue(ExternalData entity)
            {
                return entity.getSampleIdentifier();
            }
        }),

    SAMPLE_TYPE(new AbstractColumnDefinitionKind<ExternalData>(Dict.SAMPLE_TYPE, 100)
        {
            @Override
            public String tryGetValue(ExternalData entity)
            {
                return entity.getSampleType().getCode();
            }
        }),

    GROUP(new AbstractColumnDefinitionKind<ExternalData>(Dict.GROUP, 100)
        {
            @Override
            public String tryGetValue(ExternalData entity)
            {
                final Experiment exp = entity.getExperiment();
                if (exp == null)
                {
                    return null;
                }
                return exp.getProject().getGroup().getCode();
            }
        }),

    PROJECT(new AbstractColumnDefinitionKind<ExternalData>(Dict.PROJECT, 100)
        {
            @Override
            public String tryGetValue(ExternalData entity)
            {
                final Experiment exp = entity.getExperiment();
                if (exp == null)
                {
                    return null;
                }
                return exp.getProject().getCode();
            }
        }),

    EXPERIMENT(new AbstractColumnDefinitionKind<ExternalData>(Dict.EXPERIMENT, 100)
        {
            @Override
            public String tryGetValue(ExternalData entity)
            {
                final Experiment exp = entity.getExperiment();
                if (exp == null)
                {
                    return null;
                }
                return exp.getCode();
            }
        }),

    EXPERIMENT_TYPE(new AbstractColumnDefinitionKind<ExternalData>(Dict.EXPERIMENT_TYPE, 120)
        {
            @Override
            public String tryGetValue(ExternalData entity)
            {
                final Experiment experimentOrNull = entity.getExperiment();
                if (experimentOrNull == null)
                {
                    return null;
                }
                return experimentOrNull.getExperimentType().getCode();
            }

        }),

    REGISTRATION_DATE(new AbstractColumnDefinitionKind<ExternalData>(Dict.REGISTRATION_DATE, 200,
            true)
        {
            @Override
            public String tryGetValue(ExternalData entity)
            {
                return renderRegistrationDate(entity);
            }
        }),

    IS_INVALID(new AbstractColumnDefinitionKind<ExternalData>(Dict.IS_INVALID, 100, true)
        {
            @Override
            public String tryGetValue(ExternalData entity)
            {
                return renderInvalidationFlag(entity);
            }
        }),

    PERM_ID(new AbstractColumnDefinitionKind<ExternalData>(Dict.PERM_ID, true)
        {
            @Override
            public String tryGetValue(ExternalData entity)
            {
                return entity.getPermId();
            }
        }),

    SHOW_DETAILS_LINK(new AbstractColumnDefinitionKind<ExternalData>(Dict.SHOW_DETAILS_LINK, 150)
        {
            @Override
            public String tryGetValue(ExternalData entity)
            {
                return entity.getPermlink();
            }
        }),

    ;

    private final AbstractColumnDefinitionKind<ExternalData> columnDefinitionKind;

    private DataSetSearchHitColDefKind(
            AbstractColumnDefinitionKind<ExternalData> columnDefinitionKind)
    {
        this.columnDefinitionKind = columnDefinitionKind;
    }

    public String id()
    {
        return name();
    }

    public AbstractColumnDefinitionKind<ExternalData> getDescriptor()
    {
        return columnDefinitionKind;
    }

}
