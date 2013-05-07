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

import javax.swing.JComponent;
import javax.swing.JTextField;

/**
 * One line text field.
 *
 * @author Franz-Josef Elmer
 */
class TextField implements IField
{
    private JTextField textField;

    TextField()
    {
        textField = new JTextField(20);
    }

    @Override
    public JComponent getComponent()
    {
        return textField;
    }

    @Override
    public String getValue()
    {
        return textField.getText();
    }

    @Override
    public void setValue(String value)
    {
        textField.setText(value);
    }

}
