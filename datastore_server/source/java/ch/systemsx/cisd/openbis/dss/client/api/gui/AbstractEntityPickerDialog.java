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

import javax.swing.JDialog;
import javax.swing.JFrame;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public abstract class AbstractEntityPickerDialog extends JDialog
{

    protected final JFrame mainWindow;

    /**
     * @param mainWindow The parent window of thie dialog
     * @param title The title of the window
     */
    public AbstractEntityPickerDialog(JFrame mainWindow, String title)
    {
        super(mainWindow, title, true);
        this.mainWindow = mainWindow;
    }

    private static final long serialVersionUID = 1L;

}
