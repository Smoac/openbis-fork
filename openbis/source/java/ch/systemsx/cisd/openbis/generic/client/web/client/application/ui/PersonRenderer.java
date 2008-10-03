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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Person;

/**
 * @author Izabela Adamczyk
 */
public class PersonRenderer implements GridCellRenderer<ModelData>
{

    public String render(ModelData model, String property, ColumnData config, int rowIndex,
            int colIndex, ListStore<ModelData> store)
    {
        Person person = (Person) model.get(property);
        StringBuilder result = new StringBuilder();
        if (person != null)
        {
            String userId = person.getUserId();
            String firstName = person.getFirstName();
            String lastName = person.getLastName();
            if (firstName != null && lastName != null)
            {
                if (firstName != null)
                {
                    result.append(firstName + " ");
                }
                result.append(lastName);
            } else
            {
                result.append(userId);
            }
        }
        return result.toString();
    }
}
