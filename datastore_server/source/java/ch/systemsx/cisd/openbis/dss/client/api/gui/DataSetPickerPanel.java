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

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.JFrame;

import ch.systemsx.cisd.openbis.dss.client.api.v1.IOpenbisServiceFacade;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;

/**
 * @author Pawel Glyzewski
 */
public class DataSetPickerPanel extends AbstractEntityPickerPanel
{
    private static final long serialVersionUID = 1L;

    private final DataSetPickerDialog dialog;

    public DataSetPickerPanel(final JFrame mainWindow, List<Experiment> experiments,
            IOpenbisServiceFacade openbisService)
    {
        super(mainWindow, experiments, openbisService);

        dialog = new DataSetPickerDialog(mainWindow, experiments, openbisService);
    }

    public void actionPerformed(ActionEvent e)
    {
        String dataSetId = dialog.pickDataSet();
        if (dataSetId != null)
        {
            textField.setText(dataSetId);
            textField.fireActionPerformed();
        }
    }

    @Override
    protected String getButtonToolTipText()
    {
        return "Pick a Data Set";
    }
}
