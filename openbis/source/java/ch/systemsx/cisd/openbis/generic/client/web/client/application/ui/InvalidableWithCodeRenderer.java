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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;

/**
 * Renders code and marks if invalid
 * 
 * @author Tomasz Pylak
 */
public class InvalidableWithCodeRenderer implements GridCellRenderer<ModelData>
{

    public String render(final ModelData model, final String property, final ColumnData config,
            final int rowIndex, final int colIndex, final ListStore<ModelData> store)
    {
        final String code = (String) model.get(ModelDataPropertyNames.CODE);
        final boolean isInvalid = (Boolean) model.get(ModelDataPropertyNames.IS_INVALID);
        if (isInvalid)
        {
            final Element div = DOM.createDiv();
            div.setAttribute("class", "invalid");
            div.setInnerText(code);
            return DOM.toString(div);
        } else
        {
            return code;
        }
    }

}
