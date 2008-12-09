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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample_browser;

import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.GroupSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample_browser.columns.ParentColumnsConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample_browser.columns.PropertyColumnsConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Group;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleType;

/**
 * Encapsulates the logic of interaction between Sample Browser GUI elements.
 * 
 * @author Izabela Adamczyk
 */
// TODO 2008-12-05, Tomasz Pylak: use dictionary instead of hard coded strings
final class ToolbarController
{
    private final SampleTypeSelectionWidget sampleTypeSelectionWidget;

    private final GroupSelectionWidget groupSelectionWidget;

    private final CheckBox instanceCheckbox;

    private final Button submitButton;

    private final CheckBox groupCheckbox;

    private final ParentColumnsConfig parentColumns;

    private final PropertyColumnsConfig propertyColumns;

    private final Button exportButton;

    ToolbarController(final SampleTypeSelectionWidget sampleTypeSelectionWidget,
            final GroupSelectionWidget groupSelectionWidget, final CheckBox instanceCheckbox,
            final CheckBox groupCheckbox, final Button submitButton, final Button exportButton,
            final ParentColumnsConfig parentColumns, final PropertyColumnsConfig propertyColumns)
    {
        this.sampleTypeSelectionWidget = sampleTypeSelectionWidget;
        this.groupSelectionWidget = groupSelectionWidget;
        this.instanceCheckbox = instanceCheckbox;
        this.groupCheckbox = groupCheckbox;
        this.submitButton = submitButton;
        this.exportButton = exportButton;
        this.parentColumns = parentColumns;
        this.propertyColumns = propertyColumns;
    }

    /**
     * Refreshes the <i>refresh</i> resp. <i>export</i> button.
     */
    final void refreshSubmitButtons(final SampleType sampleTypeOrNull, final Group groupOrNull)
    {
        final boolean sampleTypeSelected = sampleTypeOrNull != null;
        final boolean showGroupSamples = groupCheckbox.getValue();
        final boolean groupChosen = groupOrNull != null;
        final boolean showInstanceSamples = instanceCheckbox.getValue();
        final boolean enable =
                sampleTypeSelected && (showGroupSamples && groupChosen || showInstanceSamples);
        submitButton.setEnabled(enable);
        if (enable)
        {
            submitButton.setTitle("Load or update sample table");
        } else
        {
            submitButton.setTitle("HINT: To activate select group or shared checkbox");
        }
    }

    final void enableExportButton()
    {
        exportButton.setEnabled(true);
        exportButton.setTitle("Export the sample table visible on the screen to an Excel file");
    }

    final void disableExportButton()
    {
        exportButton.setEnabled(false);
        exportButton.setTitle("Refresh the data before exporting them.");
    }

    final void showOrHideGroupList()
    {
        groupSelectionWidget.setVisible(groupCheckbox.getValue());
    }

    final void redefineColumns()
    {
        final SampleType type = sampleTypeSelectionWidget.tryGetSelectedSampleType();
        assert type != null : "Should not be null.";
        propertyColumns.define(type);
        parentColumns.define(type);
    }

    final void refreshGroupCheckbox()
    {
        final boolean atLeastOneGroupExists = groupSelectionWidget.getStore().getCount() > 0;
        groupCheckbox.setEnabled(atLeastOneGroupExists);
        groupCheckbox.setValue(atLeastOneGroupExists);
        instanceCheckbox.setValue(atLeastOneGroupExists == false);
    }
}
