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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractDataConfirmationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ManagedComboBoxInputWidgetDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Null;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedInputWidgetDescription;

public final class ManagedPropertyGridActionDialog extends
        AbstractDataConfirmationDialog<List<TableModelRowWithObject<Null>>>
{

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    @SuppressWarnings("unused")
    // to be used for actions like edit row/delete rows
    private final List<TableModelRowWithObject<Null>> data;

    private final AsyncCallback<Void> callback;

    private final IEntityInformationHolder entity;

    private final IManagedEntityProperty managedProperty;

    private final Map<String, TextField<?>> inputFieldsByLabel =
            new LinkedHashMap<String, TextField<?>>();

    public ManagedPropertyGridActionDialog(IViewContext<ICommonClientServiceAsync> viewContext,
            String editTitle, List<TableModelRowWithObject<Null>> data,
            AsyncCallback<Void> callback, IEntityInformationHolder entity,
            IManagedEntityProperty managedProperty)
    {
        super(viewContext, data, editTitle);
        this.viewContext = viewContext;
        this.data = data;
        this.entity = entity;
        this.managedProperty = managedProperty;
        this.callback = callback;
        setWidth(400);
    }

    @Override
    protected String createMessage()
    {
        // TODO 2011-01-18 - the message should depend on action and be provided by the script
        return "Action decription";
    }

    @Override
    protected void executeConfirmedAction()
    {
        StringBuilder sb = new StringBuilder();
        for (TextField<?> inputField : inputFieldsByLabel.values())
        {
            sb.append(inputField.getFieldLabel() + ": " + inputField.getValue() + "\n");
        }
        Info.display("confirmed", sb.toString());

        for (IManagedInputWidgetDescription inputDescription : managedProperty.getUiDescription()
                .getInputWidgetDescriptions())
        {
            TextField<?> field = inputFieldsByLabel.get(inputDescription.getLabel());
            inputDescription
                    .setValue(field.getValue() == null ? null : field.getValue().toString());
        }
        viewContext.getService().updateManagedProperty(TechId.create(entity),
                entity.getEntityKind(), managedProperty, callback);
    }

    @Override
    protected void extendForm()
    {
        formPanel.setLabelWidth(100);
        formPanel.setFieldWidth(200);
        for (IManagedInputWidgetDescription inputDescription : managedProperty.getUiDescription()
                .getInputWidgetDescriptions())
        {
            TextField<?> field;
            switch (inputDescription.getManagedInputFieldType())
            {
                case TEXT:
                    field = createTextField(inputDescription);
                    break;
                case COMBO_BOX:
                    field = createComboBoxField(inputDescription);
                    break;
                default:
                    throw new UnsupportedOperationException(); // can't happen
            }
            final String label = inputDescription.getLabel();
            field.setFieldLabel(label);

            if (inputDescription.getDescription() != null)
            {
                AbstractImagePrototype infoIcon =
                        AbstractImagePrototype.create(viewContext.getImageBundle().getInfoIcon());
                FieldUtil.addInfoIcon(field, inputDescription.getDescription(),
                        infoIcon.createImage());
            }
            inputFieldsByLabel.put(label, field);
            formPanel.add(field);
        }
    }

    private TextField<?> createTextField(IManagedInputWidgetDescription inputDescription)
    {
        final TextField<String> field = new TextField<String>();
        if (inputDescription.getValue() != null)
        {
            field.setValue(inputDescription.getValue());
            field.updateOriginalValue(field.getValue());
        }
        return field;
    }

    private TextField<?> createComboBoxField(IManagedInputWidgetDescription inputDescription)
    {
        final SimpleComboBox<String> comboBox = new SimpleComboBox<String>();
        comboBox.setTriggerAction(TriggerAction.ALL);
        comboBox.setEditable(false);
        comboBox.setForceSelection(true);

        if (inputDescription.getValue() != null)
        {
            // FIXME
            comboBox.setRawValue(inputDescription.getValue());
            comboBox.updateOriginalValue(comboBox.getValue());
        }

        final ManagedComboBoxInputWidgetDescription comboBoxDescription =
                (ManagedComboBoxInputWidgetDescription) inputDescription;
        comboBox.add(comboBoxDescription.getOptions());

        return comboBox;
    }
}
