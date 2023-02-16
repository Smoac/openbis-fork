/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.client.console;

import java.util.HashMap;
import java.util.Map;

import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;

/**
 * @author Franz-Josef Elmer
 */
class RegisterDataSetType extends AbstractRegisterEntityType<DataSetType> implements ICommand
{
    private enum DataSetTypeAttributeSetter implements AttributeSetter<DataSetType>
    {
        DESCRIPTION("description")
        {
            @Override
            public void setAttributeFor(DataSetType type, String value)
            {
                type.setDescription(value);

            }
        },
        PATTERN("main-pattern")
        {
            @Override
            public void setAttributeFor(DataSetType type, String value)
            {
                type.setMainDataSetPattern(value);

            }
        },
        PATH("main-path")
        {
            @Override
            public void setAttributeFor(DataSetType type, String value)
            {
                type.setMainDataSetPath(value);

            }
        };

        private final String attributeName;

        private DataSetTypeAttributeSetter(String attributeName)
        {
            this.attributeName = attributeName;
        }

        @Override
        public String getAttributeName()
        {
            return attributeName;
        }

        @Override
        public void setDefaultFor(DataSetType type)
        {
        }
    }

    private static final Map<String, AttributeSetter<DataSetType>> attributeSetters =
            new HashMap<String, AttributeSetter<DataSetType>>();

    static
    {
        for (DataSetTypeAttributeSetter attributeSetter : DataSetTypeAttributeSetter.values())
        {
            attributeSetters.put(attributeSetter.getAttributeName(), attributeSetter);
        }
    }

    @Override
    protected Map<String, AttributeSetter<DataSetType>> attributeSetters()
    {
        return attributeSetters;
    }

    @Override
    public void execute(ICommonServer server, String sessionToken, ScriptContext context,
            String argument)
    {
        server.registerDataSetType(sessionToken, prepareEntityType(new DataSetType(), argument));
    }
}
