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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.managed_property;

import java.util.List;

import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.rpc.AsyncCallback;


import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractDataConfirmationDialog;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Null;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedInputWidgetDescription;


public final class ManagedPropertyGridActionDialog extends
        AbstractDataConfirmationDialog<List<TableModelRowWithObject<Null>>>
{

    @SuppressWarnings("unused")
    private final IViewContext<ICommonClientServiceAsync> viewContext;

    @SuppressWarnings("unused")
    private final List<TableModelRowWithObject<Null>> data;

    @SuppressWarnings("unused")
    private final AsyncCallback<Void> callback;

    private final IManagedEntityProperty managedProperty;

    private List<TextField<String>> inputFields;

    public ManagedPropertyGridActionDialog(IViewContext<ICommonClientServiceAsync> viewContext,
            List<TableModelRowWithObject<Null>> data, AsyncCallback<Void> callback,
            IManagedEntityProperty managedProperty, String title)
    {
        super(viewContext, data, title);
        this.viewContext = viewContext;
        this.data = data;
        this.managedProperty = managedProperty;
        this.callback = callback;
        setWidth(400);
    }

    @Override
    protected String createMessage()
    {
        return "Update property"; // TODO
    }

    @Override
    protected void executeConfirmedAction()
    {
        StringBuilder sb = new StringBuilder();
        for (TextField<String> inputField : inputFields)
        {
            sb.append(inputField.getFieldLabel() + ": " + inputField.getValue() + "\n");
        }
        Info.display("confirmed", sb.toString());
        // TODO
        // AsyncCallback<Void> callbackWithProgressBar =
        // AsyncCallbackWithProgressBar.decorate(callback, "Deleting samples...");
        // if (selectedAndDisplayedItemsOrNull != null)
        // {
        // final DisplayedOrSelectedIdHolderCriteria<T> uploadCriteria =
        // selectedAndDisplayedItemsOrNull.createCriteria(isOnlySelected());
        // viewContext.getCommonService().deleteSamples(uploadCriteria, reason.getValue(),
        // callbackWithProgressBar);
        // } else
        // {
        // viewContext.getCommonService().deleteSample(TechId.create(singleDataOrNull),
        // reason.getValue(), callbackWithProgressBar);
        // }
    }

    @Override
    protected void extendForm()
    {
        formPanel.setLabelWidth(100);
        formPanel.setFieldWidth(200);
        for (IManagedInputWidgetDescription inputDescription : managedProperty.getUiDescription()
                .getInputWidgetDescriptions())
        {
            final TextField<String> field = new TextField<String>();
            field.setFieldLabel(inputDescription.getLabel());
            if (inputDescription.getValue() != null)
            {
                field.setValue(inputDescription.getValue());
            }
            inputFields.add(field);
            formPanel.add(field);
        }
    }

}
