/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample;

import com.extjs.gxt.ui.client.widget.button.Button;

import ch.systemsx.cisd.openbis.generic.client.shared.SampleType;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Group;

/**
 * Encapsulates the logic of interaction between Sample Browser GUI elements.
 * 
 * @author Izabela Adamczyk
 */
// TODO 2008-12-05, Tomasz Pylak: use dictionary instead of hard coded strings
final class ToolbarController
{
    private final Button submitButton;

    private final Button exportButton;

    ToolbarController(final Button submitButton, final Button exportButton)
    {
        this.submitButton = submitButton;
        this.exportButton = exportButton;
    }

    /**
     * Refreshes the <i>refresh</i> resp. <i>export</i> button.
     */
    final void refreshSubmitButtons(final SampleType sampleTypeOrNull, final Group groupOrNull)
    {
        final boolean sampleTypeSelected = sampleTypeOrNull != null;
        final boolean groupChosen = groupOrNull != null;
        final boolean enable = sampleTypeSelected && groupChosen;
        submitButton.setEnabled(enable);
        if (enable)
        {
            submitButton.setTitle("Load or update the sample table");
        } else
        {
            submitButton.setTitle("HINT: To activate select group or shared checkbox");
        }
    }

    final void enableExportButton()
    {
        exportButton.setEnabled(true);
        exportButton.setTitle("Export the table visible on the screen to an Excel file");
    }

    final void disableExportButton()
    {
        exportButton.setEnabled(false);
        exportButton.setTitle("Refresh the data before exporting them.");
    }
}
