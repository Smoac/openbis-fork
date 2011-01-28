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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto.api;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.ISerializable;

/**
 * The interface exposed to the Managed Property script.
 * <p>
 * Returns description of the input field for fluent API style with method chaining.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public interface IManagedUiDescription extends ISerializable
{
    /**
     * Sets the given table model to define an output that will be shown in detail view of the
     * entity owning the property. The table will be shown in an extra tab.
     */
    public void useTableOutput(ITableModel tableModel);

    /**
     * Adds a text input field with given <var>label</var> to input widgets that will be used in
     * user interface for modification of the managed property.
     */
    public IManagedInputWidgetDescription addTextInputField(String label);

    /**
     * Adds a multiline text with given <var>label</var> input field to input widgets that will be
     * used in user interface for modification of the managed property.
     */
    public IManagedInputWidgetDescription addMultilineTextInputField(String label);

    /**
     * Adds a combo box input field with given <var>label</var> to input widgets that will be used
     * in user interface for modification of the managed property. The combo box will contain list
     * of provided <var>values</var>.
     */
    public IManagedInputWidgetDescription addComboBoxInputField(String labels, String[] values);

    /**
     * Returns description of the widget that will be shown in detail view of the entity owning the
     * property.
     */
    public IManagedOutputWidgetDescription getOutputWidgetDescription();

    /**
     * Returns a list of objects describing input widgets that will be used in user interface user
     * interface for modification of the managed property.
     */
    public List<IManagedInputWidgetDescription> getInputWidgetDescriptions();
}
