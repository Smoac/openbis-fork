/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.util.Arrays;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.ISerializable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedUiDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedWidgetDescription;

/**
 * Object that declaratively describes a UI (labels, fields, their ordering, table content).
 * 
 * @author Piotr Buczek
 */
public class ManagedUiDescription implements IManagedUiDescription, ISerializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private IManagedWidgetDescription outputWidgetDescription;

    private List<ManagedInputWidgetDescription> inputWidgetDescriptions;

    public ManagedUiDescription()
    {
    }

    public IManagedWidgetDescription getOutputWidgetDescription()
    {
        return outputWidgetDescription;
    }

    public void setOutputWidgetDescription(IManagedWidgetDescription outputWidgetDescription)
    {
        this.outputWidgetDescription = outputWidgetDescription;
    }

    public List<ManagedInputWidgetDescription> getInputWidgetDescriptions()
    {
        return inputWidgetDescriptions;
    }

    public void setInputWidgetDescriptions(List<ManagedInputWidgetDescription> widgetDescriptions)
    {
        this.inputWidgetDescriptions = widgetDescriptions;
    }

    public void addInputWidgetDescription(ManagedInputWidgetDescription widgetDescription)
    {
        inputWidgetDescriptions.add(widgetDescription);
    }

    public void addTextInputField(String label)
    {
        ManagedTextInputWidgetDescription inputField = new ManagedTextInputWidgetDescription();
        inputField.setLabel(label);
        addInputWidgetDescription(inputField);
    }

    public void addComboBoxInputField(String label, String[] values)
    {
        ManagedComboBoxInputWidgetDescription inputField =
                new ManagedComboBoxInputWidgetDescription();
        inputField.setLabel(label);
        inputField.setOptions(Arrays.asList(values));
        addInputWidgetDescription(inputField);
    }

    public void useTableOutput(TableModel tableModel)
    {
        ManagedTableWidgetDescription tableWidget = new ManagedTableWidgetDescription();
        tableWidget.setTableModel(tableModel);
        setOutputWidgetDescription(tableWidget);
    }
}
