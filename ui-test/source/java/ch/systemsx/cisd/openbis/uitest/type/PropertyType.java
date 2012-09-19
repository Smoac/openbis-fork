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

import java.util.Map;

import ch.systemsx.cisd.openbis.uitest.infra.Browsable;
import ch.systemsx.cisd.openbis.uitest.page.common.Cell;

/**
 * @author anttil
 */
public class PropertyType implements Browsable
{

    private final String code;

    private String label;

    private String description;

    private PropertyTypeDataType dataType;

    private Vocabulary vocabulary;

    PropertyType(String code, String label, String description, PropertyTypeDataType dataType,
            Vocabulary vocabulary)
    {
        this.code = code;
        this.label = label;
        this.description = description;
        this.dataType = dataType;
        this.vocabulary = vocabulary;
    }

    @Override
    public boolean isRepresentedBy(Map<String, Cell> row)
    {
        Cell codeCell = row.get("Code");
        return codeCell != null && codeCell.getText().equalsIgnoreCase(this.code);
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof PropertyType))
        {
            return false;
        }

        PropertyType type = (PropertyType) o;
        return type.getCode().equals(this.code);
    }

    @Override
    public int hashCode()
    {
        return this.getCode().hashCode();
    }

    @Override
    public String toString()
    {
        return "PropertyType " + code + " of type " + dataType;
    }

    public String getCode()
    {
        return code;
    }

    public String getLabel()
    {
        return label;
    }

    public String getDescription()
    {
        return description;
    }

    public PropertyTypeDataType getDataType()
    {
        return dataType;
    }

    public Vocabulary getVocabulary()
    {
        return vocabulary;
    }

    void setLabel(String label)
    {
        this.label = label;
    }

    void setDescription(String description)
    {
        this.description = description;
    }

    void setDataType(PropertyTypeDataType dataType)
    {
        this.dataType = dataType;
    }

    void setVocabulary(Vocabulary vocabulary)
    {
        this.vocabulary = vocabulary;
    }

}
