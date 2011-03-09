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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.SimpleComboValue;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.MultilineVarcharField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractDataConfirmationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.lang.StringEscapeUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ManagedComboBoxInputWidgetDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ManagedTableWidgetDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ManagedUiTableActionDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ReportRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedInputWidgetDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedUiTableAction;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.ManagedTableActionRowSelectionType;

public final class ManagedPropertyGridActionDialog extends
        AbstractDataConfirmationDialog<List<TableModelRowWithObject<ReportRowModel>>>
{

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private final List<TableModelRowWithObject<ReportRowModel>> data;

    private final AsyncCallback<Void> callback;

    private final IEntityInformationHolder entity;

    private final IManagedProperty managedProperty;

    private final IManagedUiTableAction managedAction;

    private final Map<String, TextField<?>> inputFieldsByLabel =
            new LinkedHashMap<String, TextField<?>>();

    public ManagedPropertyGridActionDialog(IViewContext<ICommonClientServiceAsync> viewContext,
            String editTitle, List<TableModelRowWithObject<ReportRowModel>> data,
            AsyncCallback<Void> callback, IEntityInformationHolder entity,
            IManagedProperty managedProperty, IManagedUiTableAction managedAction)
    {
        super(viewContext, data, editTitle);
        this.viewContext = viewContext;
        this.data = data;
        this.entity = entity;
        this.managedProperty = managedProperty;
        this.callback = callback;
        this.managedAction = managedAction;
        setWidth(400);
    }

    @Override
    protected String createMessage()
    {
        return managedAction.getDescription() == null ? "Provide data" : managedAction
                .getDescription();
    }

    @Override
    protected void executeConfirmedAction()
    {
        if (managedAction instanceof ManagedUiTableActionDescription)
        {
            ManagedUiTableActionDescription ma = (ManagedUiTableActionDescription) managedAction;
            List<Integer> selectedRows = new ArrayList<Integer>();
            for (TableModelRowWithObject<ReportRowModel> rowModel : data)
            {
                selectedRows.add(rowModel.getObjectOrNull().getRowNumber());
            }
            Collections.sort(selectedRows);
            ma.setSelectedRows(selectedRows);
        }

        for (IManagedInputWidgetDescription inputDescription : managedAction
                .getInputWidgetDescriptions())
        {
            TextField<?> field = inputFieldsByLabel.get(inputDescription.getLabel());
            Object fieldValue = field.getValue();
            String value = fieldValue == null ? null : field.getValue().toString();
            if (fieldValue instanceof SimpleComboValue)
            {
                value = ((SimpleComboValue<?>) fieldValue).getValue().toString();
            }
            inputDescription.setValue(value);
        }
        // old value was escaped going to the client - unescape it before sending back to the server
        managedProperty.setValue(StringEscapeUtils.unescapeHtml(managedProperty.getValue()));
        viewContext.getService().updateManagedProperty(TechId.create(entity),
                entity.getEntityKind(), managedProperty, managedAction, callback);
    }

    @Override
    protected void extendForm()
    {
        formPanel.setLabelWidth(100);
        formPanel.setFieldWidth(200);

        for (IManagedInputWidgetDescription inputDescription : managedAction
                .getInputWidgetDescriptions())
        {
            trySetBoundedValue(inputDescription);
            TextField<?> field;
            switch (inputDescription.getManagedInputFieldType())
            {
                case TEXT:
                    field = createTextField(inputDescription);
                    break;
                case MULTILINE_TEXT:
                    field = createMultilineTextField(inputDescription);
                    break;
                case COMBO_BOX:
                    field = createComboBoxField(inputDescription);
                    break;
                default:
                    throw new UnsupportedOperationException(); // can't happen
            }
            final String label = inputDescription.getLabel();
            if (label == null)
            {
                throwFailToCreateContentException("Label is not set in input widget description");
            }
            field.setFieldLabel(label);

            if (inputDescription.getDescription() != null)
            {
                AbstractImagePrototype infoIcon =
                        AbstractImagePrototype.create(viewContext.getImageBundle().getInfoIcon());
                FieldUtil.addInfoIcon(field, inputDescription.getDescription(),
                        infoIcon.createImage());
            }
            FieldUtil.setMandatoryFlag(field, inputDescription.isMandatory());

            inputFieldsByLabel.put(label, field);
            formPanel.add(field);
        }
    }

    /**
     * If the managed action requires single row to be selected and there is a binding between given
     * input field and a table column it tries to get the value from the selected table row and sets
     * it in the input model.
     */
    private void trySetBoundedValue(IManagedInputWidgetDescription inputDescription)
    {
        if (managedAction.getSelectionType() == ManagedTableActionRowSelectionType.REQUIRED_SINGLE
                && data.size() == 1)
        {
            String boundedColumnTitleOrNull =
                    managedAction.getBindings().get(inputDescription.getLabel());
            if (boundedColumnTitleOrNull != null)
            {
                if (viewContext.isLoggingEnabled())
                {
                    Info.display("found binding", inputDescription.getLabel() + "->"
                            + boundedColumnTitleOrNull);
                }
                TableModelRowWithObject<ReportRowModel> selectedRow = data.get(0);
                TableModel tableModel =
                        ((ManagedTableWidgetDescription) managedProperty.getUiDescription()
                                .getOutputWidgetDescription()).getTableModel();

                for (TableModelColumnHeader header : tableModel.getHeader())
                {
                    if (header.getTitle().equals(boundedColumnTitleOrNull))
                    {
                        ISerializableComparable value =
                                selectedRow.getValues().get(header.getIndex());
                        inputDescription.setValue(value.toString());
                        if (viewContext.isLoggingEnabled())
                        {
                            Info.display("bounded value", inputDescription.getLabel() + "=" + value);
                        }
                        break;
                    }
                }
            }
        }
    }

    private TextField<?> createTextField(IManagedInputWidgetDescription inputDescription)
    {
        final TextField<String> field = new TextField<String>();
        if (inputDescription.getValue() != null)
        {
            FieldUtil.setValueWithUnescaping(field, inputDescription.getValue());
            field.updateOriginalValue(field.getValue());
        }
        return field;
    }

    private TextField<?> createMultilineTextField(IManagedInputWidgetDescription inputDescription)
    {
        final TextField<String> field =
                new MultilineVarcharField(inputDescription.getLabel(), false);
        if (inputDescription.getValue() != null)
        {
            FieldUtil.setValueWithUnescaping(field, inputDescription.getValue());
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
        if (inputDescription instanceof ManagedComboBoxInputWidgetDescription)
        {
            final ManagedComboBoxInputWidgetDescription comboBoxDescription =
                    (ManagedComboBoxInputWidgetDescription) inputDescription;
            comboBox.add(comboBoxDescription.getOptions());

            if (inputDescription.getValue() != null)
            {
                comboBox.setSimpleValue(inputDescription.getValue());
                comboBox.updateOriginalValue(comboBox.getValue());
            }
            return comboBox;
        } else
        {
            throwFailToCreateContentException("'" + inputDescription.getLabel()
                    + "' description should be a subclass of ManagedComboBoxInputWidgetDescription");
            return null;
        }
    }

    private void throwFailToCreateContentException(String detailedErrorMsg)
            throws UserFailureException
    {
        throw new UserFailureException("Failed to create content.", detailedErrorMsg);
    }

}
