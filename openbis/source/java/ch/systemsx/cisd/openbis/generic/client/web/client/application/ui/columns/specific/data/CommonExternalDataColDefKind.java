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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.renderers.SimpleYesNoRenderer;
import ch.systemsx.cisd.openbis.generic.shared.basic.SimpleDateRenderer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;

/**
 * @author     Franz-Josef Elmer
 */
public enum CommonExternalDataColDefKind implements IColumnDefinitionKind<ExternalData>
{
    CODE(new AbstractColumnDefinitionKind<ExternalData>(Dict.CODE)
        {
            @Override
            public String tryGetValue(ExternalData entity)
            {
                return entity.getCode();
            }
        }),

    SAMPLE(new AbstractColumnDefinitionKind<ExternalData>(Dict.SAMPLE, 100, true)
        {
            @Override
            public String tryGetValue(ExternalData entity)
            {
                return entity.getSampleCode();
            }
        }),

    SAMPLE_IDENTIFIER(new AbstractColumnDefinitionKind<ExternalData>(
            Dict.EXTERNAL_DATA_SAMPLE_IDENTIFIER, 200)
        {
            @Override
            public String tryGetValue(ExternalData entity)
            {
                return entity.getSampleIdentifier();
            }
        }),

    SAMPLE_TYPE(new AbstractColumnDefinitionKind<ExternalData>(Dict.SAMPLE_TYPE)
        {
            @Override
            public String tryGetValue(ExternalData entity)
            {
                final SampleType typeOrNull = entity.getSampleType();
                return typeOrNull == null ? null : typeOrNull.getCode();
            }
        }),

    EXPERIMENT(new AbstractColumnDefinitionKind<ExternalData>(Dict.EXPERIMENT, 100, true)
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

    EXPERIMENT_IDENTIFIER(new AbstractColumnDefinitionKind<ExternalData>(
            Dict.EXTERNAL_DATA_EXPERIMENT_IDENTIFIER, 100, true)
        {
            @Override
            public String tryGetValue(ExternalData entity)
            {
                final Experiment exp = entity.getExperiment();
                if (exp == null)
                {
                    return null;
                }
                return exp.getIdentifier();
            }
        }),

    EXPERIMENT_TYPE(new AbstractColumnDefinitionKind<ExternalData>(Dict.EXPERIMENT_TYPE, 120, true)
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
            false)
        {
            @Override
            public String tryGetValue(ExternalData entity)
            {
                return renderRegistrationDate(entity);
            }
        }),

    IS_INVALID(new AbstractColumnDefinitionKind<ExternalData>(Dict.IS_INVALID, true)
        {
            @Override
            public String tryGetValue(ExternalData entity)
            {
                return renderInvalidationFlag(entity);
            }
        }),

    SOURCE_TYPE(new AbstractColumnDefinitionKind<ExternalData>(Dict.SOURCE_TYPE, true)
        {
            @Override
            public String tryGetValue(ExternalData entity)
            {
                return entity.getSourceType();
            }
        }),

    IS_COMPLETE(new AbstractColumnDefinitionKind<ExternalData>(Dict.IS_COMPLETE, true)
        {
            @Override
            public String tryGetValue(ExternalData entity)
            {
                Boolean complete = entity.getComplete();
                return complete == null ? "?" : SimpleYesNoRenderer.render(complete);
            }
        }),

    LOCATION(new AbstractColumnDefinitionKind<ExternalData>(Dict.LOCATION, true)
        {
            @Override
            public String tryGetValue(ExternalData entity)
            {
                return entity.getLocation();
            }
        }),

    FILE_FORMAT_TYPE(new AbstractColumnDefinitionKind<ExternalData>(Dict.FILE_FORMAT_TYPE, true)
        {
            @Override
            public String tryGetValue(ExternalData entity)
            {
                FileFormatType fileFormatType = entity.getFileFormatType();
                return fileFormatType == null ? null : fileFormatType.getCode();
            }
        }),

    DATA_SET_TYPE(new AbstractColumnDefinitionKind<ExternalData>(Dict.DATA_SET_TYPE, true)
        {
            @Override
            public String tryGetValue(ExternalData entity)
            {
                return entity.getDataSetType().getCode();
            }
        }),

    PRODUCTION_DATE(new AbstractColumnDefinitionKind<ExternalData>(Dict.PRODUCTION_DATE, 200, true)
        {
            @Override
            public String tryGetValue(ExternalData entity)
            {
                return SimpleDateRenderer.renderDate(entity.getProductionDate());
            }
        }),

    DATA_PRODUCER_CODE(
            new AbstractColumnDefinitionKind<ExternalData>(Dict.DATA_PRODUCER_CODE, true)
                {
                    @Override
                    public String tryGetValue(ExternalData entity)
                    {
                        return entity.getDataProducerCode();
                    }
                }),

    DATA_STORE_CODE(new AbstractColumnDefinitionKind<ExternalData>(Dict.DATA_STORE_CODE, true)
        {
            @Override
            public String tryGetValue(ExternalData entity)
            {
                return entity.getDataStore().getCode();
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

    SHOW_DETAILS_LINK(new AbstractColumnDefinitionKind<ExternalData>(Dict.SHOW_DETAILS_LINK, true)
        {
            @Override
            public String tryGetValue(ExternalData entity)
            {
                return entity.getPermlink();
            }
        });

    private final AbstractColumnDefinitionKind<ExternalData> columnDefinitionKind;

    private CommonExternalDataColDefKind(
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
