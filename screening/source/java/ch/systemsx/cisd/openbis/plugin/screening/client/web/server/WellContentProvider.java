/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.server;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.server.ITableModelProvider;
import ch.systemsx.cisd.openbis.generic.server.util.TypedTableModelBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;
import ch.systemsx.cisd.openbis.plugin.screening.shared.IScreeningServer;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateMaterialsSearchCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellContent;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class WellContentProvider implements ITableModelProvider<WellContent>
{
    static final String WELL_CONTENT_MATERIAL_ID = "WELL_CONTENT_MATERIAL";
    static final String WELL_CONTENT_MATERIAL_TYPE_ID = "WELL_CONTENT_MATERIAL_TYPE";
    static final String WELL_CONTENT_PROPERTY_ID_PREFIX = "WELL_CONTENT_PROPERTY-";
    
    private final IScreeningServer server;
    private final String sessionToken;
    private final PlateMaterialsSearchCriteria materialCriteria;
    private TypedTableModel<WellContent> model;

    WellContentProvider(IScreeningServer server, String sessionToken, PlateMaterialsSearchCriteria materialCriteria)
    {
        this.server = server;
        this.sessionToken = sessionToken;
        this.materialCriteria = materialCriteria;
    }

    public TypedTableModel<WellContent> getTableModel()
    {
        if (model == null)
        {
            TypedTableModelBuilder<WellContent> builder = new TypedTableModelBuilder<WellContent>();
            builder.addColumn(WELL_CONTENT_MATERIAL_ID);
            List<WellContent> wells = server.listPlateWells(sessionToken, materialCriteria);
            for (WellContent well : wells)
            {
                builder.addRow(well);
                String value = well.getMaterialContent().getCode();
                builder.addStringValueToColumn(WELL_CONTENT_MATERIAL_ID, value);
                List<IEntityProperty> properties = well.getMaterialContent().getProperties();
                for (IEntityProperty property : properties)
                {
                    PropertyType propertyType = property.getPropertyType();
                    String code = propertyType.getCode();
                    builder.addStringValueToColumn(propertyType.getLabel(),
                            WELL_CONTENT_PROPERTY_ID_PREFIX + code, property.tryGetAsString());
                }
            }
            model = builder.getModel();
        }
        return model;
    }
}
