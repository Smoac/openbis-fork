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

package ch.systemsx.cisd.openbis.knime.common;

import java.io.Serializable;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.port.PortObjectSpec;

import ch.systemsx.cisd.openbis.plugin.query.client.api.v1.IQueryApiFacade;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public abstract class AbstractDescriptionBasedNodeDialog<D extends Serializable> extends AbstractOpenBisNodeDialog
{
    private JComboBox descriptionComboBox;

    protected AbstractDescriptionBasedNodeDialog(String tabTitle)
    {
        super(tabTitle);
    }

    @Override
    protected void defineQueryForm(JPanel queryPanel)
    {
        descriptionComboBox = new JComboBox();
        defineQueryForm(queryPanel, descriptionComboBox);
    }

    @Override
    protected void updateQueryForm(IQueryApiFacade facade)
    {
        List<D> descriptions = getSortedDescriptions(facade);
        D selectedQueryDescription = getSelectedDescriptionOrNull();
        descriptionComboBox.removeAllItems();
        for (D description : descriptions)
        {
            descriptionComboBox.addItem(description);
            if (description.equals(selectedQueryDescription))
            {
                descriptionComboBox.setSelectedItem(description);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    protected D getSelectedDescriptionOrNull()
    {
        Object selectedItem = descriptionComboBox.getSelectedItem();
        return selectedItem == null ? null : (D) selectedItem;
    }

    @Override
    protected void loadAdditionalSettingsFrom(NodeSettingsRO settings, PortObjectSpec[] specs)
            throws NotConfigurableException
    {
        byte[] bytes = settings.getByteArray(getDescriptionKey(), null);
        D descriptionOrNull = Util.deserializeDescription(bytes);
        if (descriptionOrNull != null && descriptionComboBox.getItemCount() == 0)
        {
            descriptionComboBox.addItem(descriptionOrNull);
            descriptionComboBox.setSelectedIndex(0);
        }
        loadMoreSettingsFrom(settings, specs);
    }

    @Override
    protected void saveAdditionalSettingsTo(NodeSettingsWO settings)
            throws InvalidSettingsException
    {
        byte[] bytes = Util.serializeDescription(getSelectedDescriptionOrNull());
        settings.addByteArray(getDescriptionKey(), bytes);
        saveMoreSettingsTo(settings);
    }

    protected abstract void defineQueryForm(JPanel queryPanel, JComboBox comboBoxWithDescriptions);

    protected abstract List<D> getSortedDescriptions(IQueryApiFacade facade);
    
    protected abstract String getDescriptionKey();
    
    protected abstract void loadMoreSettingsFrom(NodeSettingsRO settings, PortObjectSpec[] specs)
            throws NotConfigurableException;
    
    protected abstract void saveMoreSettingsTo(NodeSettingsWO settings)
            throws InvalidSettingsException;
}
