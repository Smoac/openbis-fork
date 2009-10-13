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

import com.extjs.gxt.ui.client.data.ModelData;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.PropertyTypeColDefKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;

/**
 * {@link ModelData} for {@link PropertyType}.
 * 
 * @author Izabela Adamczyk
 * @author Tomasz Pylak
 */
public class PropertyTypeModel extends BaseEntityModel<PropertyType>
{
    private static final long serialVersionUID = 1L;

    public PropertyTypeModel(GridRowModel<PropertyType> entity,
            IColumnDefinitionKind<PropertyType>[] colDefKinds)
    {
        super(entity, colDefKinds);

        overwriteTypes(PropertyTypeColDefKind.EXPERIMENT_TYPES);
        overwriteTypes(PropertyTypeColDefKind.MATERIAL_TYPES);
        overwriteTypes(PropertyTypeColDefKind.SAMPLE_TYPES);
    }

    // changes the column value from the export format to the display format
    private void overwriteTypes(PropertyTypeColDefKind columnKind)
    {
        String columnId = columnKind.id();
        String commaValues = get(columnId);
        String newValue = rerenderTypes(commaValues);
        set(columnId, newValue);
    }

    // transforms comma separated value list by replacing each comma with a div element. It results
    // in one item displayed per line.
    private static String rerenderTypes(String commaValues)
    {
        String[] tokens = commaValues.split(",");
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < tokens.length; i++)
        {
            final Element div = DOM.createDiv();
            div.setInnerText(tokens[i].trim());
            sb.append(div.getString());
        }
        String newValue = sb.toString();
        return newValue;
    }
}
