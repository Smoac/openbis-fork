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
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;

import ch.systemsx.cisd.openbis.generic.client.shared.PropertyType;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IColumnDefinitionUI;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property_type.PropertyTypeColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;

/**
 * {@link ModelData} for {@link PropertyType}.
 * 
 * @author Izabela Adamczyk
 */
public class PropertyTypeModel extends AbstractEntityModel<PropertyType>
{
    private static final long serialVersionUID = 1L;

    public PropertyTypeModel(PropertyType entity)
    {
        super(entity, createColumnsSchema(null));

        overwriteTypes(PropertyTypeColDefKind.EXPERIMENT_TYPES);
        overwriteTypes(PropertyTypeColDefKind.MATERIAL_TYPES);
        overwriteTypes(PropertyTypeColDefKind.SAMPLE_TYPES);
    }

    private void overwriteTypes(PropertyTypeColDefKind columnKind)
    {
        String columnId = columnKind.id();
        String commaValues = get(columnId);
        String newValue = rerenderTypes(commaValues);
        set(columnId, newValue);
    }

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

    public static List<IColumnDefinitionUI<PropertyType>> createColumnsSchema(
            IMessageProvider msgProviderOrNull)
    {
        return createColumnsSchemaFrom(PropertyTypeColDefKind.values(), msgProviderOrNull);
    }

    public final static List<PropertyTypeModel> convert(final List<PropertyType> types)
    {
        final List<PropertyTypeModel> result = new ArrayList<PropertyTypeModel>();
        for (final PropertyType st : types)
        {
            result.add(new PropertyTypeModel(st));
        }
        return result;
    }

}
