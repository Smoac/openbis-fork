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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.LinkExtractor;
import ch.systemsx.cisd.openbis.generic.shared.basic.SimpleDateRenderer;
import ch.systemsx.cisd.openbis.generic.shared.basic.SimpleYesNoRenderer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;

/**
 * @author Franz-Josef Elmer
 */
public enum CommonExternalDataColDefKind implements IColumnDefinitionKind<ExternalData>
{
    CODE(new AbstractColumnDefinitionKind<ExternalData>(Dict.CODE, 150)
        {
            @Override
            public String tryGetValue(ExternalData entity)
            {
                return entity.getCode();
            }

            @Override
            public String tryGetLink(ExternalData entity)
            {
                return LinkExtractor.tryExtract(entity);
            }
        }),

    DATA_SET_TYPE(new AbstractColumnDefinitionKind<ExternalData>(Dict.DATA_SET_TYPE, 200)
        {
            @Override
            public String tryGetValue(ExternalData entity)
            {
                return entity.getDataSetType().getCode();
            }
        }),

    CONTAINER(new AbstractColumnDefinitionKind<ExternalData>(Dict.CONTAINER_DATA_SET, 150, true)
        {
            @Override
            public String tryGetValue(ExternalData entity)
            {
                final ExternalData containerOrNull = entity.tryGetContainer();
                return containerOrNull != null ? containerOrNull.getCode() : "";
            }

            @Override
            public String tryGetLink(ExternalData entity)
            {
                final ExternalData containerOrNull = entity.tryGetContainer();
                return LinkExtractor.tryExtract(containerOrNull);
            }
        }),

    ORDER_IN_CONTAINER(new AbstractColumnDefinitionKind<ExternalData>(Dict.ORDER_IN_CONTAINER, 100,
            true)
        {
            @Override
            public String tryGetValue(ExternalData entity)
            {
                final Integer orderOrNull = entity.getOrderInContainer();
                return orderOrNull != null ? orderOrNull.toString() : "";
            }
        }),

    SAMPLE(new AbstractColumnDefinitionKind<ExternalData>(Dict.SAMPLE, 100, true)
        {
            @Override
            public String tryGetValue(ExternalData entity)
            {
                return entity.getSampleCode();
            }

            @Override
            public String tryGetLink(ExternalData entity)
            {
                return LinkExtractor.tryExtract(entity);
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

            @Override
            public String tryGetLink(ExternalData entity)
            {
                return LinkExtractor.tryExtract(entity.getSample());
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
                return exp.getCode();
            }

            @Override
            public String tryGetLink(ExternalData entity)
            {
                return LinkExtractor.tryExtract(entity.getExperiment());
            }
        }),

    EXPERIMENT_IDENTIFIER(new AbstractColumnDefinitionKind<ExternalData>(
            Dict.EXTERNAL_DATA_EXPERIMENT_IDENTIFIER, 100, true)
        {
            @Override
            public String tryGetValue(ExternalData entity)
            {
                final Experiment exp = entity.getExperiment();
                return exp.getIdentifier();
            }

            @Override
            public String tryGetLink(ExternalData entity)
            {
                return LinkExtractor.tryExtract(entity.getExperiment());
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

    PROJECT(new AbstractColumnDefinitionKind<ExternalData>(Dict.PROJECT)
        {
            @Override
            public String tryGetValue(ExternalData entity)
            {
                final Experiment exp = entity.getExperiment();
                return exp.getProject().getCode();
            }

            @Override
            public String tryGetLink(ExternalData entity)
            {
                final Experiment exp = entity.getExperiment();
                return LinkExtractor.tryExtract(exp.getProject());
            }
        }),

    REGISTRATOR(new AbstractColumnDefinitionKind<ExternalData>(Dict.REGISTRATOR)
        {
            @Override
            public String tryGetValue(ExternalData entity)
            {
                return renderRegistrator(entity);
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

    IS_DELETED(new AbstractColumnDefinitionKind<ExternalData>(Dict.IS_DELETED, true)
        {
            @Override
            public String tryGetValue(ExternalData entity)
            {
                return renderDeletionFlag(entity);
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

    IS_COMPLETE(new AbstractDataSetColumnDefinitionKind(Dict.IS_COMPLETE, true)
        {
            @Override
            public String tryGetValue(DataSet dataSet)
            {
                Boolean complete = dataSet.getComplete();
                return complete == null ? "?" : SimpleYesNoRenderer.render(complete);
            }
        }),

    LOCATION(new AbstractDataSetColumnDefinitionKind(Dict.LOCATION, true)
        {
            @Override
            public String tryGetValue(DataSet dataSet)
            {
                return dataSet.getFullLocation();
            }
        }),

    STATUS(new AbstractDataSetColumnDefinitionKind(Dict.ARCHIVING_STATUS, 200, true)
        {
            @Override
            public String tryGetValue(DataSet entity)
            {
                return entity.getStatus().getDescription();
            }
        }),

    PRESENT_IN_ARCHIVE(new AbstractDataSetColumnDefinitionKind(Dict.PRESENT_IN_ARCHIVE, true)
        {
            @Override
            public String tryGetValue(DataSet entity)
            {
                return SimpleYesNoRenderer.render(entity.isPresentInArchive());
            }
        }),

    FILE_FORMAT_TYPE(new AbstractDataSetColumnDefinitionKind(Dict.FILE_FORMAT_TYPE, true)
        {
            @Override
            public String tryGetValue(DataSet dataSet)
            {
                FileFormatType fileFormatType = dataSet.getFileFormatType();
                return fileFormatType == null ? null : fileFormatType.getCode();
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
        }),

    OVERVIEW(new AbstractColumnDefinitionKind<ExternalData>(Dict.OVERVIEW, true)
        {
            @Override
            public String tryGetValue(ExternalData entity)
            {
                return ""; // link doesn't make sense here as session id is needed
            }
        });

    private abstract static class AbstractDataSetColumnDefinitionKind extends
            AbstractColumnDefinitionKind<ExternalData>
    {
        public AbstractDataSetColumnDefinitionKind(String headerMsgKey, boolean isHidden)
        {
            super(headerMsgKey, isHidden);
        }

        public AbstractDataSetColumnDefinitionKind(String headerMsgKey, int width, boolean isHidden)
        {
            super(headerMsgKey, width, isHidden);
        }

        @Override
        public String tryGetValue(ExternalData entity)
        {
            DataSet dataSet = entity.tryGetAsDataSet();
            if (dataSet != null)
            {
                return tryGetValue(dataSet);
            } else
            {
                return null;
            }
        }

        abstract String tryGetValue(DataSet dataSet);

    }

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
