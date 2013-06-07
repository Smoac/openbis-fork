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

package ch.systemsx.cisd.openbis.knime.common;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO.DataSetOwnerType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.plugin.query.client.api.v1.IQueryApiFacade;

/**
 * One-line text field for an identifier for an entity of type Experiment, Sample or Data Set. 
 * An additional button allows to use {@link EntityChooser}.
 * 
 * @author Franz-Josef Elmer
 */
class EntityField implements IField
{
    private final DataSetOwnerType ownerType;
    
    private final IQueryApiFacadeProvider facadeProvider;
    
    private final JPanel textFieldWithButton;

    private final JTextField ownerField;

    private final IAsyncNodeAction asyncNodeAction;

    EntityField(DataSetOwnerType entityType, IQueryApiFacadeProvider facadeProvider, IAsyncNodeAction asyncNodeAction)
    {
        this.ownerType = entityType;
        this.facadeProvider = facadeProvider;
        this.asyncNodeAction = asyncNodeAction;
        textFieldWithButton = new JPanel(new BorderLayout());
        ownerField = new JTextField(20);
        textFieldWithButton.add(ownerField, BorderLayout.CENTER);
        JButton button = new JButton("...");
        button.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    chooseOwner();
                }
            });
        textFieldWithButton.add(button, BorderLayout.EAST);
    }

    @Override
    public JComponent getComponent()
    {
        return textFieldWithButton;
    }

    @Override
    public String getValue()
    {
        return ownerField.getText();
    }

    @Override
    public void setValue(String value)
    {
        ownerField.setText(value);
    }

    private void chooseOwner()
    {
        IQueryApiFacade facade = facadeProvider.getQueryFacade();
        String sessionToken = facade.getSessionToken();
        IGeneralInformationService service = facade.getGeneralInformationService();
        String ownerOrNull =
                new EntityChooser(ownerField, ownerType, false, sessionToken, service, asyncNodeAction).getOwnerOrNull();
        if (ownerOrNull != null)
        {
            ownerField.setText(ownerOrNull);
        }

    }

}
