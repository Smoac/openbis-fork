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

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.widget.form.ComboBox;

/**
 * @author Izabela Adamczyk
 */
public class ExtendedComboBox<T extends ModelData> extends ComboBox<T>
{
    /**
     * Selects given <var>value</var> of given <var>comboBox</var>.
     */
    public final void setSelectedItem(final String property, final String value)
    {
        List<T> list = getStore().findModels(property, value);
        if (list == null || list.size() == 0)
        {
            throw new IllegalArgumentException("Element " + value + " not in the list.");
        }
        List<T> selection = new ArrayList<T>();
        selection.add(list.get(0));
        setSelection(selection);
    }

}
