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

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ParentColumns;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyColumns;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleType;

/**
 * Encapsulates the logic of interaction between Sample Browser GUI elements.
 * 
 * @author Izabela Adamczyk
 */
public class ToolbarController
{

    private final SampleTypeSelectionWidget sampleTypeSelectionWidget;

    private final GroupSelectionWidget groupSelectionWidget;

    private final CheckBox instanceCheckbox;

    private final Button submitButton;

    private final CheckBox groupCheckbox;

    private final ColumnChooser columnChooser;

    private final ParentColumns parentColumns;

    private final PropertyColumns propertyColumns;

    private final Button exportButton;

    public ToolbarController(SampleTypeSelectionWidget sampleTypeSelectionWidget,
            GroupSelectionWidget groupSelectionWidget, CheckBox instanceCheckbox,
            CheckBox groupCheckbox, Button submitButton, Button exportButton,
            ColumnChooser columnChooser, ParentColumns parentColumns,
            PropertyColumns propertyColumns)
    {
        this.sampleTypeSelectionWidget = sampleTypeSelectionWidget;
        this.groupSelectionWidget = groupSelectionWidget;
        this.instanceCheckbox = instanceCheckbox;
        this.groupCheckbox = groupCheckbox;
        this.submitButton = submitButton;
        this.exportButton = exportButton;
        this.columnChooser = columnChooser;
        this.parentColumns = parentColumns;
        this.propertyColumns = propertyColumns;
    }

    public void refreshButtons()
    {
        final boolean sampleTypeSelected = sampleTypeSelectionWidget.tryGetSelected() != null;
        final boolean showGroupSamples = groupCheckbox.getValue();
        final boolean groupChosen = groupSelectionWidget.tryGetSelected() != null;
        final boolean showInstanceSamples = instanceCheckbox.getValue() == true;
        final boolean enable =
                sampleTypeSelected && (showGroupSamples && groupChosen || showInstanceSamples);
        submitButton.setEnabled(enable);
        exportButton.setEnabled(enable);

    }

    public void showOrHideGroupList()
    {
        groupSelectionWidget.setVisible(groupCheckbox.getValue());
    }

    public void rebuildColumnChooser()
    {
        final SampleType type = sampleTypeSelectionWidget.tryGetSelected();
        if (type != null)
        {
            propertyColumns.define(type);
            parentColumns.define(type);
            columnChooser.reload();
            columnChooser.setEnabled(true);
        }
    }

    public void resetPropertyCache(boolean reset)
    {
        if (reset)
        {
            propertyColumns.resetLoaded();
            columnChooser.reload();
            columnChooser.setEnabled(true);
        }
    }

    public void refreshGroupCheckbox()
    {
        final boolean atLeastOneGroupExists = groupSelectionWidget.getStore().getCount() > 0;
        groupCheckbox.setEnabled(atLeastOneGroupExists);
        groupCheckbox.setValue(atLeastOneGroupExists);
        instanceCheckbox.setValue(atLeastOneGroupExists == false);
    }
}
