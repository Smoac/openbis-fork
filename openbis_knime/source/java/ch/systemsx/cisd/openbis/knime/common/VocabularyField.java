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

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;

import org.apache.commons.lang.StringUtils;

/**
 * Combo box with a list of String terms. 
 *
 * @author Franz-Josef Elmer
 */
class VocabularyField implements IField
{
    private JComboBox comboBox;

    public VocabularyField(String fieldParameters)
    {
        String[] splittedParameters = fieldParameters.split(",");
        List<String> terms = new ArrayList<String>();
        for (String term : splittedParameters)
        {
            if (StringUtils.isNotBlank(term))
            {
                terms.add(term.trim());
            }
        }
        comboBox = new JComboBox(terms.toArray());
        comboBox.setEditable(false);
    }

    @Override
    public JComponent getComponent()
    {
        return comboBox;
    }

    @Override
    public String getValue()
    {
        return (String) comboBox.getSelectedItem();
    }

    @Override
    public void setValue(String value)
    {
        comboBox.setSelectedItem(value);
    }

}
