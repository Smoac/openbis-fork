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

package ch.systemsx.cisd.openbis.knime.server;

import java.util.HashSet;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.IRowBuilderAdaptor;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.ISimpleTableModelBuilderAdaptor;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class ParameterDescriptionsBuilder
{
    private final ISimpleTableModelBuilderAdaptor tableBuilder;
    
    private final Set<String> names = new HashSet<String>();

    ParameterDescriptionsBuilder(ISimpleTableModelBuilderAdaptor tableBuilder)
    {
        this.tableBuilder = tableBuilder;
        tableBuilder.addHeader(Constants.PARAMETER_DESCRIPTION_NAME_COLUMN);
        tableBuilder.addHeader(Constants.PARAMETER_DESCRIPTION_TYPE_COLUMN);
    }
    
    public ParameterDescriptionBuilder parameter(String name)
    {
        if (names.contains(name))
        {
            throw new IllegalArgumentException("There is already a parameter with name '" + name + "'.");
        }
        names.add(name);
        IRowBuilderAdaptor row = tableBuilder.addRow();
        row.setCell(Constants.PARAMETER_DESCRIPTION_NAME_COLUMN, name);
        return new ParameterDescriptionBuilder(row);
    }
    
}
