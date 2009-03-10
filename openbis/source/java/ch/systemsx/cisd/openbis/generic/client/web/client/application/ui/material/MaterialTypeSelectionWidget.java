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

import java.util.List;

import com.extjs.gxt.ui.client.widget.form.ComboBox;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.MaterialTypeModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.DropDownList;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;

/**
 * {@link ComboBox} containing list of material types loaded from the server.
 * 
 * @author Izabela Adamczyk
 */
public final class MaterialTypeSelectionWidget extends
        DropDownList<MaterialTypeModel, MaterialType>
{
    public static final String SUFFIX = "material-type";

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    public MaterialTypeSelectionWidget(final IViewContext<ICommonClientServiceAsync> viewContext,
            final String idSuffix)
    {
        super(viewContext, SUFFIX + idSuffix, Dict.MATERIAL_TYPE, ModelDataPropertyNames.CODE,
                "material type", "material types");
        this.viewContext = viewContext;
    }

    /**
     * Returns the {@link MaterialType} currently selected.
     * 
     * @return <code>null</code> if nothing is selected yet.
     */
    public final MaterialType tryGetSelectedMaterialType()
    {
        return super.tryGetSelected();
    }

    @Override
    protected List<MaterialTypeModel> convertItems(List<MaterialType> result)
    {
        return MaterialTypeModel.convert(result);
    }

    @Override
    protected void loadData(AbstractAsyncCallback<List<MaterialType>> callback)
    {
        viewContext.getService().listMaterialTypes(callback);
    }
}