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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.DropDownList;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;

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

    private final String additionalOptionLabelOrNull;

    /**
     * Creates a material type chooser with one additional option. It's useful when you want to have
     * one special value on the list.
     */
    public static MaterialTypeSelectionWidget createWithAdditionalOption(
            final IViewContext<ICommonClientServiceAsync> viewContext,
            final String additionalOptionLabel, final String idSuffix)
    {
        return new MaterialTypeSelectionWidget(viewContext, additionalOptionLabel, idSuffix);
    }

    public static MaterialTypeSelectionWidget createWithInitialValue(
            final IViewContext<ICommonClientServiceAsync> viewContext,
            final MaterialType initValueOrNull, final String idSuffix)
    {
        MaterialTypeSelectionWidget chooser =
                new MaterialTypeSelectionWidget(viewContext, null, idSuffix);
        if (initValueOrNull != null)
        {
            chooser.setValue(new MaterialTypeModel(initValueOrNull));
        }
        return chooser;
    }

    public MaterialTypeSelectionWidget(final IViewContext<ICommonClientServiceAsync> viewContext,
            final String idSuffix)
    {
        this(viewContext, null, idSuffix);
    }

    private MaterialTypeSelectionWidget(IViewContext<ICommonClientServiceAsync> viewContext,
            String additionalOptionLabelOrNull, String idSuffix)
    {
        super(viewContext, SUFFIX + idSuffix, Dict.MATERIAL_TYPE, ModelDataPropertyNames.CODE,
                "material type", "material types");
        this.viewContext = viewContext;
        this.additionalOptionLabelOrNull = additionalOptionLabelOrNull;
        setTemplate(GWTUtils.getTooltipTemplate(ModelDataPropertyNames.CODE,
                ModelDataPropertyNames.TOOLTIP));
    }

    /**
     * Returns the {@link MaterialType} currently selected.
     * 
     * @return <code>null</code> if nothing is selected yet or "none" option has been chosen (if it
     *         has been enabled before).
     */
    public final MaterialType tryGetSelectedMaterialType()
    {
        return super.tryGetSelected();
    }

    /** @return true if none option has been enabled and chosen */
    public final boolean isAdditionalOptionSelected()
    {
        return includeAdditionalOption() && isAnythingSelected() && tryGetSelected() == null;
    }

    @Override
    protected List<MaterialTypeModel> convertItems(List<MaterialType> result)
    {
        if (includeAdditionalOption())
        {
            return MaterialTypeModel.convertWithAdditionalOption(result,
                    additionalOptionLabelOrNull);
        } else
        {
            return MaterialTypeModel.convert(result);
        }
    }

    private boolean includeAdditionalOption()
    {
        return additionalOptionLabelOrNull != null;
    }

    @Override
    protected void loadData(AbstractAsyncCallback<List<MaterialType>> callback)
    {
        viewContext.getService().listMaterialTypes(callback);
    }

    public DatabaseModificationKind[] getRelevantModifications()
    {
        return DatabaseModificationKind.any(ObjectKind.MATERIAL_TYPE);
    }
}
