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
 * <pre>
 * action has
 * - name (both for display and usage)
 * - description (for tooltip/message shown in dialog)
 * </pre>
 * <p>
 * All methods of this interface are part of the Managed Properties API.
 * 
 * @author Piotr Buczek
 */
public interface IManagedUiAction extends ISerializable
{
    /** Returns string identifier of the action. */
    public String getName();

    /** Returns string describing the action behaviour. */
    public String getDescription();

    /**
     * Sets a string describing the action behaviour.
     * 
     * @return this (for method chaining)
     */
    public IManagedUiAction setDescription(String description);

    /**
     * Adds a text input field with given <var>label</var> to input widgets that will be used in
     * user interface for modifcation of a managed property.
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
     * Returns a list of objects describing input widgets that will be used in user interface user
     * interface for modification of the managed property.
     */
    public List<IManagedInputWidgetDescription> getInputWidgetDescriptions();

    /**
     * Convenience method returning value of input widget with given label or null if such widget
     * doesn't exist.
     */
    public String getInputValue(String inputLabel);
}
