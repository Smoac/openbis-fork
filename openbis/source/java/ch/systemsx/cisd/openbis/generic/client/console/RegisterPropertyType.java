/*
 * Copyright 2010 ETH Zuerich, CISD
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

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;

/**
 * @author Franz-Josef Elmer
 */
public class RegisterPropertyType implements ICommand
{
    private static final String WITH = "with";

    private static final String DATA = "data";

    private final static String TYPE = "type";

    @SuppressWarnings("unused")
    private static final String LABEL = ", label = ";

    private static boolean validated(List<String> tokens)
    {
        return tokens.size() > 4 && tokens.get(1).equals(WITH) && tokens.get(2).equals(DATA)
                && tokens.get(3).equals(TYPE);
    }

    public void execute(ICommonServer server, String sessionToken, ScriptContext context,
            String argument)
    {
        List<String> tokens = Lexer.extractTokens(argument);

        if (false == validated(tokens))
        {
            throw new IllegalArgumentException(
                    "syntax error: expected '{property} with data type {data-type}', got: "
                            + argument);
        }

        PropertyType propertyType = new PropertyType();
        propertyType.setCode(tokens.get(0));
        propertyType.setLabel(tokens.get(0));
        propertyType.setDescription(" ");

        if (tokens.get(4).startsWith(DataTypeCode.MATERIAL.name()))
        {
            propertyType.setDataType(new DataType(DataTypeCode.MATERIAL));
            if (DataTypeCode.MATERIAL.name().length() < tokens.get(4).length())
            {
                String materialCode =
                        tokens.get(4).replaceAll(DataTypeCode.MATERIAL.name() + "\\((.*)\\)", "$1");
                MaterialType materialType = server.getMaterialType(sessionToken, materialCode);
                propertyType.setMaterialType(materialType);
            }
        } else
        {
            DataTypeCode dataTypeCode = DataTypeCode.valueOf(tokens.get(4));
            propertyType.setDataType(new DataType(dataTypeCode));
        }

        server.registerPropertyType(sessionToken, propertyType);
    }
}
