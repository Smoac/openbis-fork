/*
 * Copyright 2009 ETH Zuerich, CISD
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

import com.extjs.gxt.ui.client.widget.form.RadioGroup;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.material.MaterialBrowserGrid.DisplayedAndSelectedMaterials;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractDataListDeletionConfirmationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.WidgetUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DisplayedOrSelectedIdHolderCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;

public final class MaterialListDeletionConfirmationDialog extends
        AbstractDataListDeletionConfirmationDialog<Material>
{

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private final AbstractAsyncCallback<Void> callback;

    private final DisplayedAndSelectedMaterials selectedAndDisplayedItems;

    public MaterialListDeletionConfirmationDialog(
            IViewContext<ICommonClientServiceAsync> viewContext, List<Material> data,
            AbstractAsyncCallback<Void> callback,
            DisplayedAndSelectedMaterials selectedAndDisplayedItems)
    {
        super(viewContext, data, true);
        this.viewContext = viewContext;
        this.callback = callback;
        this.selectedAndDisplayedItems = selectedAndDisplayedItems;
    }

    @Override
    protected void executeConfirmedAction()
    {
        final DisplayedOrSelectedIdHolderCriteria<Material> uploadCriteria =
                selectedAndDisplayedItems.createCriteria(isOnlySelected());
        viewContext.getCommonService().deleteMaterials(uploadCriteria, reason.getValue(), callback);

    }

    @Override
    protected String getEntityName()
    {
        return EntityKind.MATERIAL.getDescription();
    }

    @Override
    protected final RadioGroup createRadio()
    {
        return WidgetUtils.createAllOrSelectedRadioGroup(onlySelectedRadioOrNull =
                WidgetUtils.createRadio(viewContext.getMessage(Dict.ONLY_SELECTED_RADIO, data
                        .size())), WidgetUtils.createRadio(viewContext.getMessage(Dict.ALL_RADIO,
                selectedAndDisplayedItems.getDisplayedItemsCount())), viewContext
                .getMessage(Dict.MATERIALS_RADIO_GROUP_LABEL), data.size(),
                createRefreshMessageAction());
    }

}
