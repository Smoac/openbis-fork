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

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleType;

/**
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

    public ToolbarController(SampleTypeSelectionWidget sampleTypeSelectionWidget,
            GroupSelectionWidget groupSelectionWidget, CheckBox instanceCheckbox,
            CheckBox groupCheckbox, Button submitButton, ColumnChooser columnChooser)
    {
        this.sampleTypeSelectionWidget = sampleTypeSelectionWidget;
        this.groupSelectionWidget = groupSelectionWidget;
        this.instanceCheckbox = instanceCheckbox;
        this.groupCheckbox = groupCheckbox;
        this.submitButton = submitButton;
        this.columnChooser = columnChooser;
    }

    public void refreshSubmitButton()
    {
        final boolean sampleTypeSelected = sampleTypeSelectionWidget.isValid();
        final boolean showGroupSamples = groupCheckbox.getValue();
        final boolean groupChosen = groupSelectionWidget.isValid();
        final boolean showInstanceSamples = instanceCheckbox.getValue() == true;
        submitButton.setEnabled(sampleTypeSelected
                && (showGroupSamples && groupChosen || showInstanceSamples));
    }

    public void showOrHideGroupList()
    {
        groupSelectionWidget.setVisible(groupCheckbox.getValue());
    }

    public void refreshColumnChooser()
    {
        final SampleType type = sampleTypeSelectionWidget.tryGetSelected();
        if (type != null)
        {
            columnChooser.load(type);
            columnChooser.setEnabled(true);
        }
    }
}
