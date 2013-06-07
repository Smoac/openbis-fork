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

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.openbis.knime.server.FieldType;

/**
 * Description necessary to create a {@link IField}.
 *
 * @author Franz-Josef Elmer
 */
public class FieldDescription
{
    private final String name;
    private final FieldType fieldType;
    private final String fieldParameters;

    public FieldDescription(String name, FieldType fieldType, String fieldParameters)
    {
        this.name = name;
        this.fieldType = fieldType;
        this.fieldParameters = fieldParameters;
    }

    public String getName()
    {
        return name;
    }

    public FieldType getFieldType()
    {
        return fieldType;
    }

    public String getFieldParameters()
    {
        return fieldParameters;
    }

    @Override
    public String toString()
    {
        return name + ":" + fieldType + (StringUtils.isBlank(fieldParameters) ? "" : "[" + fieldParameters + "]");
    }
    
}
