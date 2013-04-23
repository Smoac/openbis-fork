/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.knime.file;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.port.PortObjectSpec;

import ch.systemsx.cisd.openbis.dss.client.api.v1.IOpenbisServiceFacade;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO.DataSetOwnerType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetType;
import ch.systemsx.cisd.openbis.knime.common.AbstractOpenBisNodeDialog;
import ch.systemsx.cisd.openbis.plugin.query.client.api.v1.IQueryApiFacade;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class DataSetRegistrationNodeDialog extends AbstractOpenBisNodeDialog
{
    private JComboBox ownerTypeComboBox;
    private JTextField ownerField;
    private final IOpenbisServiceFacadeFactory serviceFacadeFactory;
    private JComboBox dataSetTypeComboBox;

    protected DataSetRegistrationNodeDialog(IOpenbisServiceFacadeFactory serviceFacadeFactory)
    {
        super("Data Set Registration Settings");
        this.serviceFacadeFactory = serviceFacadeFactory;
    }

    @Override
    protected void defineQueryForm(JPanel queryPanel)
    {
        JPanel fields = new JPanel(new GridBagLayout());
        ownerTypeComboBox = new JComboBox(DataSetOwnerType.values());
        ownerTypeComboBox.addItemListener(new ItemListener()
            {
                @Override
                public void itemStateChanged(ItemEvent event)
                {
                    Object item = event.getItem();
                    logger.info(item.getClass());
                }
            });
        addField(fields, "Owner type", ownerTypeComboBox);
        ownerField = new JTextField(20);
        addField(fields, "Owner", ownerField);
        dataSetTypeComboBox = new JComboBox();
        addField(fields, "Data Set Type", dataSetTypeComboBox);
        queryPanel.add(fields, BorderLayout.NORTH);
    }

    @Override
    protected void updateQueryForm(IQueryApiFacade facade)
    {
        dataSetTypeComboBox.removeAllItems();
        List<DataSetType> dataSetTypes = createOpenbisFacade().listDataSetTypes();
        for (DataSetType dataSetType : dataSetTypes)
        {
            dataSetTypeComboBox.addItem(dataSetType.getCode());
        }
    }

    @Override
    protected void loadAdditionalSettingsFrom(NodeSettingsRO settings, PortObjectSpec[] specs)
            throws NotConfigurableException
    {
        ownerTypeComboBox.setSelectedItem(DataSetOwnerType.valueOf(settings.getString(
                DataSetRegistrationNodeModel.OWNER_TYPE_KEY, DataSetOwnerType.EXPERIMENT.name())));
        ownerField.setText(settings.getString(DataSetRegistrationNodeModel.OWNER_KEY, ""));
        String dataSetType =
                settings.getString(DataSetRegistrationNodeModel.DATA_SET_TYPE_KEY, null);
        if (dataSetType != null && dataSetTypeComboBox.getItemCount() == 0)
        {
            dataSetTypeComboBox.addItem(dataSetType);
            dataSetTypeComboBox.setSelectedIndex(0);
        }
    }

    @Override
    protected void saveAdditionalSettingsTo(NodeSettingsWO settings)
            throws InvalidSettingsException
    {
        settings.addString(DataSetRegistrationNodeModel.OWNER_TYPE_KEY,
                ((DataSetOwnerType) ownerTypeComboBox.getSelectedItem()).name());
        settings.addString(DataSetRegistrationNodeModel.OWNER_KEY, ownerField.getText());
        settings.addString(DataSetRegistrationNodeModel.DATA_SET_TYPE_KEY, dataSetTypeComboBox
                .getSelectedItem().toString());
    }

    private IOpenbisServiceFacade createOpenbisFacade()
    {
        try
        {
            String url = urlField.getText();
            String userID = userField.getText();
            String password = new String(passwordField.getPassword());
            return serviceFacadeFactory.createFacade(url, userID, password);
        } catch (RuntimeException ex)
        {
            showException(ex);
            throw ex;
        }
    }

}
