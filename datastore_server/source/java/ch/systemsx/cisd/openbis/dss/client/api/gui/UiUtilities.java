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

package ch.systemsx.cisd.openbis.dss.client.api.gui;

import java.awt.Color;

import javax.swing.JComponent;
import javax.swing.JLabel;

import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.validation.ValidationError;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class UiUtilities
{

    public static void displayError(JLabel label, JComponent component,
            ErrorsPanel errorAreaOrNull, ValidationError error)
    {
        component.setToolTipText(error.getErrorMessage());
        label.setForeground(Color.RED);
        if (errorAreaOrNull != null)
        {
            errorAreaOrNull.reportError(error);
        }
    }

    public static void clearError(JLabel label, JComponent component, ErrorsPanel errorAreaOrNull)
    {
        component.setToolTipText(null);
        label.setForeground(Color.BLACK);
        if (errorAreaOrNull != null)
        {
            errorAreaOrNull.clear();
        }
    }

    static final String WAITING_NODE_LABEL = "Loading data ...";
}
