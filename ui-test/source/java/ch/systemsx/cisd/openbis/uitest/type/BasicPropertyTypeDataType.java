/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.uitest.type;

/**
 * @author anttil
 */
public enum BasicPropertyTypeDataType implements PropertyTypeDataType
{
    BOOLEAN("BOOLEAN"),
    HYPERLINK("HYPERLINK"),
    INTEGER("INTEGER"),
    MATERIAL("MATERIAL"),
    MULTILINE_VARCHAR("MULTILINE_VARCHAR"),
    REAL("REAL"),
    TIMESTAMP("TIMESTAMP"),
    VARCHAR("VARCHAR"),
    XML("XML");

    private String name;

    private BasicPropertyTypeDataType(String name)
    {
        this.name = name;
    }

    @Override
    public String getName()
    {
        return this.name;
    }
}
