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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DataSetSearchHit;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Procedure;

/**
 * Definition of data set search results table columns.
 * 
 * @author Izabela Adamczyk
 */
public enum DataSetSearchHitColDefKind implements IColumnDefinitionKind<DataSetSearchHit>
{

    CODE(new AbstractColumnDefinitionKind<DataSetSearchHit>(Dict.CODE, 200)
        {
            @Override
            public String tryGetValue(DataSetSearchHit entity)
            {
                return entity.getDataSet().getCode();
            }
        }),

    PARENT_CODE(new AbstractColumnDefinitionKind<DataSetSearchHit>(Dict.PARENT_CODE, 200, true)
        {
            @Override
            public String tryGetValue(DataSetSearchHit entity)
            {
                return entity.getDataSet().getParentCode();
            }
        }),

    LOCATION(new AbstractColumnDefinitionKind<DataSetSearchHit>(Dict.LOCATION, 200)
        {
            @Override
            public String tryGetValue(DataSetSearchHit entity)
            {
                return entity.getDataSet().getLocation();
            }
        }),

    DATA_SET_TYPE(new AbstractColumnDefinitionKind<DataSetSearchHit>(Dict.DATA_SET_TYPE, 120)
        {
            @Override
            public String tryGetValue(DataSetSearchHit entity)
            {
                return entity.getDataSet().getDataSetType().getCode();
            }
        }),

    FILE_TYPE(new AbstractColumnDefinitionKind<DataSetSearchHit>(Dict.FILE_FORMAT_TYPE, 120)
        {
            @Override
            public String tryGetValue(DataSetSearchHit entity)
            {
                return entity.getDataSet().getFileFormatType().getCode();
            }
        }),

    SAMPLE(new AbstractColumnDefinitionKind<DataSetSearchHit>(Dict.SAMPLE, 100)
        {
            @Override
            public String tryGetValue(DataSetSearchHit entity)
            {
                return entity.getDataSet().getSampleCode();
            }
        }),

    SAMPLE_IDENTIFIER(new AbstractColumnDefinitionKind<DataSetSearchHit>(Dict.SAMPLE_IDENTIFIER,
            true)
        {
            @Override
            public String tryGetValue(DataSetSearchHit entity)
            {
                return entity.getDataSet().getSampleIdentifier();
            }
        }),

    SAMPLE_TYPE(new AbstractColumnDefinitionKind<DataSetSearchHit>(Dict.SAMPLE_TYPE, 100)
        {
            @Override
            public String tryGetValue(DataSetSearchHit entity)
            {
                return entity.getDataSet().getSampleType().getCode();
            }
        }),

    GROUP(new AbstractColumnDefinitionKind<DataSetSearchHit>(Dict.GROUP, 100)
        {
            @Override
            public String tryGetValue(DataSetSearchHit entity)
            {
                final Experiment exp = tryGetExperiment(entity);
                if (exp == null)
                {
                    return null;
                }
                return exp.getProject().getGroup().getCode();
            }
        }),

    PROJECT(new AbstractColumnDefinitionKind<DataSetSearchHit>(Dict.PROJECT, 100)
        {
            @Override
            public String tryGetValue(DataSetSearchHit entity)
            {
                final Experiment exp = tryGetExperiment(entity);
                if (exp == null)
                {
                    return null;
                }
                return exp.getProject().getCode();
            }
        }),

    EXPERIMENT(new AbstractColumnDefinitionKind<DataSetSearchHit>(Dict.EXPERIMENT, 100)
        {
            @Override
            public String tryGetValue(DataSetSearchHit entity)
            {
                final Experiment exp = tryGetExperiment(entity);
                if (exp == null)
                {
                    return null;
                }
                return exp.getCode();
            }
        }),

    EXPERIMENT_TYPE(new AbstractColumnDefinitionKind<DataSetSearchHit>(Dict.EXPERIMENT_TYPE, 120)
        {
            @Override
            public String tryGetValue(DataSetSearchHit entity)
            {
                final Experiment experimentOrNull = tryGetExperiment(entity);
                if (experimentOrNull == null)
                {
                    return null;
                }
                return experimentOrNull.getExperimentType().getCode();
            }

        }),

    REGISTRATION_DATE(new AbstractColumnDefinitionKind<DataSetSearchHit>(Dict.REGISTRATION_DATE,
            200, true)
        {
            @Override
            public String tryGetValue(DataSetSearchHit entity)
            {
                return renderRegistrationDate(entity.getDataSet());
            }
        }),

    IS_INVALID(new AbstractColumnDefinitionKind<DataSetSearchHit>(Dict.IS_INVALID, 100, true)
        {
            @Override
            public String tryGetValue(DataSetSearchHit entity)
            {
                return renderInvalidationFlag(entity.getDataSet());
            }
        }),

    ;

    // TODO 2009-02-09 Tomasz Pylak: show EXPERIMENT_PROPERTY, SAMPLE_PROPERTY

    private final AbstractColumnDefinitionKind<DataSetSearchHit> columnDefinitionKind;

    private DataSetSearchHitColDefKind(
            AbstractColumnDefinitionKind<DataSetSearchHit> columnDefinitionKind)
    {
        this.columnDefinitionKind = columnDefinitionKind;
    }

    public String id()
    {
        return name();
    }

    public AbstractColumnDefinitionKind<DataSetSearchHit> getDescriptor()
    {
        return columnDefinitionKind;
    }

    private static Experiment tryGetExperiment(DataSetSearchHit entity)
    {
        final Procedure procetureOrNull = tryGetProceture(entity);
        if (procetureOrNull == null)
        {
            return null;
        }
        return procetureOrNull.getExperiment();
    }

    private static Procedure tryGetProceture(DataSetSearchHit entity)
    {
        return entity.getDataSet().getProcedure();
    }

}
