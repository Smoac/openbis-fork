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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.material;

import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.widget.toolbar.AdapterToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Element;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListMaterialCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;

/**
 * The toolbar of material browser.
 * 
 * @author Izabela Adamczyk
 */
class MaterialBrowserToolbar extends ToolBar
{
    public static final String ID = "material-browser-toolbar";

    private final MaterialTypeSelectionWidget selectMaterialTypeCombo;

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    public MaterialBrowserToolbar(final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        this.viewContext = viewContext;
        selectMaterialTypeCombo = new MaterialTypeSelectionWidget(viewContext, ID);
        display();
    }

    public void setCriteriaChangedListener(SelectionChangedListener<?> criteriaChangedListener)
    {
        selectMaterialTypeCombo.addSelectionChangedListener(criteriaChangedListener);
    }

    private void display()
    {
        setBorders(true);
        add(new LabelToolItem(viewContext.getMessage(Dict.MATERIAL_TYPE)
                + GenericConstants.LABEL_SEPARATOR));
        add(new AdapterToolItem(selectMaterialTypeCombo));
    }

    public final ListMaterialCriteria tryGetCriteria()
    {
        final MaterialType selectedType = selectMaterialTypeCombo.tryGetSelectedMaterialType();
        if (selectedType == null)
        {
            return null;
        }
        ListMaterialCriteria criteria = new ListMaterialCriteria();
        criteria.setMaterialType(selectedType);
        return criteria;
    }

    @Override
    protected final void onRender(final Element parent, final int pos)
    {
        super.onRender(parent, pos);
    }

}