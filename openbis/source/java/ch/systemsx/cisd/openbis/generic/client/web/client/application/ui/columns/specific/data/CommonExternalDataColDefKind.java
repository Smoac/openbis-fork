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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.renderers.SimpleDateRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.renderers.SimpleYesNoRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalData;

/**
 * @author Franz-Josef Elmer
 */
public enum CommonExternalDataColDefKind implements IColumnDefinitionKind<ExternalData>
{
    CODE(new AbstractColumnDefinitionKind<ExternalData>(Dict.CODE)
        {
            @Override
            public String tryGetValue(ExternalData entity)
            {
                return renderLink(entity.getCode());
            }
        }),

    PARENT_CODE(new AbstractColumnDefinitionKind<ExternalData>(Dict.PARENT_CODE, true)
        {
            @Override
            public String tryGetValue(ExternalData entity)
            {
                return entity.getParentCode();
            }
        }),

    PRODECUDRE_TYPE(new AbstractColumnDefinitionKind<ExternalData>(Dict.PROCEDURE_TYPE)
        {
            @Override
            public String tryGetValue(ExternalData entity)
            {
                return entity.getProcedureType().getCode();
            }
        }),

    SAMPLE_IDENTIFIER(new AbstractColumnDefinitionKind<ExternalData>(Dict.EXTERNAL_DATA_SAMPLE,
            200, false)
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
                return entity.getSampleType().getCode();
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

    IS_DERIVED(new AbstractColumnDefinitionKind<ExternalData>(Dict.IS_DERIVED, true)
        {
            @Override
            public String tryGetValue(ExternalData entity)
            {
                return SimpleYesNoRenderer.render(entity.isDerived());
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

    LOCATION(new AbstractColumnDefinitionKind<ExternalData>(Dict.LOCATION)
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
                return entity.getFileFormatType().getCode();
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
