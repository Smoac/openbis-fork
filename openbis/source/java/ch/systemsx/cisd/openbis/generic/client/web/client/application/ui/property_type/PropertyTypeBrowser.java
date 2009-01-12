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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property_type;

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Element;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.CommonViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.PropertyTypeModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.GridWithRPCProxy;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyType;

/**
 * Encapsulates property types listing functionality.
 * 
 * @author Izabela Adamczyk
 */
public class PropertyTypeBrowser extends ContentPanel
{

    public static final String ID = GenericConstants.ID_PREFIX + "property-types-browser";

    private GridWithRPCProxy<PropertyType, PropertyTypeModel> grid;

    public PropertyTypeBrowser(final CommonViewContext viewContext)
    {
        setLayout(new FitLayout());
        setHeading("Property types");
        setId(ID);
        grid = new PropertyTypeGrid(viewContext, ID);
        add(grid);
    }

    @Override
    protected void onRender(final Element parent, final int pos)
    {
        super.onRender(parent, pos);
        layout();
        grid.load();
    }

}
