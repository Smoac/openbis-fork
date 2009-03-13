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
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.LinkRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.ColumnConfigFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.AbstractColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.data.CommonExternalDataColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalData;

/**
 * {@link ModelData} for {@link ExternalData}.
 * 
 * @author Christian Ribeaud
 */
public final class ExternalDataModel extends BaseEntityModel<ExternalData>
{

    private static final long serialVersionUID = 1L;

    public ExternalDataModel(final ExternalData externalData)
    {
        super(externalData, CommonExternalDataColDefKind.values());
        String columnID = CommonExternalDataColDefKind.CODE.id();
        set(columnID, LinkRenderer.renderAsLink(String.valueOf(get(columnID))));
    }

    /**
     * Creates column model from all definitions.
     */
    public static ColumnModel createColumnModel(IMessageProvider messageProvider)
    {
        CommonExternalDataColDefKind[] values = CommonExternalDataColDefKind.values();
        List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
        for (CommonExternalDataColDefKind colDefKind : values)
        {
            AbstractColumnDefinitionKind<?> colDesc = colDefKind.getDescriptor();
            String header = messageProvider.getMessage(colDesc.getHeaderMsgKey());
            String id = colDefKind.id();
            ColumnConfig columnConfig = ColumnConfigFactory.createDefaultColumnConfig(header, id);
            columnConfig.setHidden(colDesc.isHidden());
            columnConfig.setWidth(colDesc.getWidth());
            configs.add(columnConfig);
        }
        return new ColumnModel(configs);
    }

    public static ColumnDefsAndConfigs<ExternalData> createColumnsSchema(
            IMessageProvider messageProvider)
    {
        return BaseEntityModel.createColumnConfigs(CommonExternalDataColDefKind.values(),
                messageProvider);
    }
}
