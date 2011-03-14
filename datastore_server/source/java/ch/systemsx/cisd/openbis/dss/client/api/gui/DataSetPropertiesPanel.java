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

import static ch.systemsx.cisd.openbis.dss.client.api.gui.DataSetUploadClient.BUTTON_HEIGHT;
import static ch.systemsx.cisd.openbis.dss.client.api.gui.DataSetUploadClient.BUTTON_WIDTH;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import ch.systemsx.cisd.openbis.dss.client.api.gui.DataSetUploadClientModel.NewDataSetInfo;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTOBuilder;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetMetadataDTO;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.PropertyTypeGroup;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class DataSetPropertiesPanel extends JPanel
{
    private static final long serialVersionUID = 1L;

    private final DataSetType dataSetType;

    private final DataSetUploadClientModel clientModel;

    private final HashMap<String, JTextField> formFields = new HashMap<String, JTextField>();

    private NewDataSetInfo newDataSetInfo;

    public DataSetPropertiesPanel(DataSetType dataSetType, DataSetUploadClientModel clientModel)
    {
        super();
        this.dataSetType = dataSetType;
        this.clientModel = clientModel;
        setLayout(new GridBagLayout());
        createGui();
    }

    public NewDataSetInfo getDataSetInfo()
    {
        return newDataSetInfo;
    }

    public void setNewDataSetInfo(NewDataSetInfo dataSetInfo)
    {
        this.newDataSetInfo = dataSetInfo;
        syncGui();
    }

    private void createGui()
    {
        // Add the fields, two per row, to the GUI; ignore the groups for now
        int row = 0;
        int col = 0;
        List<PropertyTypeGroup> groups = dataSetType.getPropertyTypeGroups();
        for (PropertyTypeGroup group : groups)
        {
            for (PropertyType propertyType : group.getPropertyTypes())
            {
                addFormFieldForPropertyType(row, col, propertyType);
                if (++col > 1)
                {
                    // advance to the next row
                    ++row;
                    col = 0;
                }
            }
        }
    }

    private void addFormFieldForPropertyType(int row, int col, final PropertyType propertyType)
    {
        JLabel label = new JLabel(propertyType.getLabel() + ":", JLabel.TRAILING);
        label.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));

        final JTextField textField = new JTextField();
        textField.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    setPropertyValue(propertyType, textField.getText());
                }

            });
        textField.addFocusListener(new FocusListener()
            {
                public void focusLost(FocusEvent e)
                {
                    setPropertyValue(propertyType, textField.getText());
                }

                public void focusGained(FocusEvent e)
                {
                    // Do nothing
                }
            });
        addFormField(col, row, label, textField);
        formFields.put(propertyType.getCode(), textField);
    }

    private void addFormField(int colx, int rowy, Component label, Component field)
    {
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        // Internally, we consume two columns, one for the label, one for the field
        c.gridx = colx * 2;
        c.gridy = rowy;
        c.weightx = 0;
        c.weighty = 0;
        c.insets = new Insets((rowy > 0) ? 5 : 0, 0, 0, 5);
        add(label, c);
        c.gridx = c.gridx + 1;
        c.weightx = 0.5;
        c.insets = new Insets((rowy > 0) ? 5 : 0, 0, 0, 5);
        add(field, c);
    }

    private void setPropertyValue(PropertyType propertyType, String text)
    {
        if (null == newDataSetInfo)
        {
            return;
        }

        NewDataSetDTOBuilder builder = newDataSetInfo.getNewDataSetBuilder();
        NewDataSetMetadataDTO metadata = builder.getDataSetMetadata();
        Map<String, String> props = metadata.getProperties();
        HashMap<String, String> newProps = new HashMap<String, String>(props);
        if (null == text || text.trim().length() < 1)
        {
            newProps.remove(propertyType.getCode());
        } else
        {
            newProps.put(propertyType.getCode(), text);
        }
        metadata.setProperties(newProps);

        clientModel.notifyObserversOfChanges(newDataSetInfo);
    }

    private void syncGui()
    {
        NewDataSetDTOBuilder builder = newDataSetInfo.getNewDataSetBuilder();
        NewDataSetMetadataDTO metadata = builder.getDataSetMetadata();
        Map<String, String> props = metadata.getProperties();
        for (String propertyTypeCode : formFields.keySet())
        {
            JTextField textField = formFields.get(propertyTypeCode);
            textField.setText(props.get(propertyTypeCode));
        }
    }

}
