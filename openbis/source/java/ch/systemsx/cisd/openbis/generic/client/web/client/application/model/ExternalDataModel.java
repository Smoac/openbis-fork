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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.model;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.data.ModelData;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.columns.CommonExternalDataColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IColumnDefinitionUI;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IColumnDefinition;

/**
 * {@link ModelData} for {@link ExternalData}.
 * 
 * @author Christian Ribeaud
 */
public final class ExternalDataModel extends AbstractEntityModel<ExternalData>
{

    private static final long serialVersionUID = 1L;

    public ExternalDataModel(final ExternalData externalData)
    {
        // TODO 2009-01-19, Tomasz Pylak: define column schema
        // super(externalData, createColumnsSchema(null));
        super(externalData, new ArrayList<IColumnDefinition<ExternalData>>());
        set(ModelDataPropertyNames.CODE, externalData.getCode());
        set(ModelDataPropertyNames.LOCATION, externalData.getLocation());
        set(ModelDataPropertyNames.FILE_FORMAT_TYPE, externalData.getFileFormatType().getCode());
    }

    public static List<IColumnDefinitionUI<ExternalData>> createColumnsSchema(
            IMessageProvider msgProviderOrNull)
    {
        return createColumnsSchemaFrom(CommonExternalDataColDefKind.values(), msgProviderOrNull);
    }

    public final static List<ExternalDataModel> asExternalDataModels(final List<ExternalData> result)
    {
        final List<ExternalDataModel> models = new ArrayList<ExternalDataModel>(result.size());
        for (final ExternalData externalData : result)
        {
            models.add(new ExternalDataModel(externalData));
        }
        return models;
    }
}
