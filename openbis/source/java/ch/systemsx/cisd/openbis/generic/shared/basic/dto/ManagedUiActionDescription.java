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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.ISerializable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedInputWidgetDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedUiAction;

/**
 * Object that declaratively describes a UI for an action (e.g. describing UI of a dialog that
 * should be shown after clicking on a button).
 * 
 * @author Piotr Buczek
 */
public class ManagedUiActionDescription implements IManagedUiAction, ISerializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    String name;

    private String description;

    private List<IManagedInputWidgetDescription> inputWidgets =
            new ArrayList<IManagedInputWidgetDescription>();

    // for serialization
    public ManagedUiActionDescription()
    {
    }

    public ManagedUiActionDescription(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    public IManagedUiAction setDescription(String aDescription)
    {
        this.description = aDescription;
        return this;
    }

    public List<IManagedInputWidgetDescription> getInputWidgetDescriptions()
    {
        return inputWidgets;
    }

    public void setInputWidgetDescriptions(List<IManagedInputWidgetDescription> widgetDescriptions)
    {
        this.inputWidgets = widgetDescriptions;
    }

    public void addInputWidgetDescription(IManagedInputWidgetDescription widgetDescription)
    {
        inputWidgets.add(widgetDescription);
    }

    public IManagedInputWidgetDescription addTextInputField(String label)
    {
        ManagedTextInputWidgetDescription inputField = new ManagedTextInputWidgetDescription();
        inputField.setLabel(label);
        addInputWidgetDescription(inputField);
        return inputField;
    }

    public IManagedInputWidgetDescription addMultilineTextInputField(String label)
    {
        ManagedMultilineTextInputWidgetDescription inputField =
                new ManagedMultilineTextInputWidgetDescription();
        inputField.setLabel(label);
        addInputWidgetDescription(inputField);
        return inputField;
    }

    public IManagedInputWidgetDescription addComboBoxInputField(String label, String[] values)
    {
        ManagedComboBoxInputWidgetDescription inputField =
                new ManagedComboBoxInputWidgetDescription();
        inputField.setLabel(label);
        inputField.setOptions(Arrays.asList(values));
        addInputWidgetDescription(inputField);
        return inputField;
    }

    public String getInputValue(String inputLabel)
    {
        for (IManagedInputWidgetDescription inputDescription : getInputWidgetDescriptions())
        {
            if (inputDescription.getLabel().equals(inputLabel))
            {
                return inputDescription.getValue();
            }
        }
        return null;
    }

}
