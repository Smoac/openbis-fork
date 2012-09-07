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
import java.util.UUID;

import ch.systemsx.cisd.openbis.uitest.infra.Browsable;

/**
 * @author anttil
 */
public class PropertyType implements Browsable
{

    private String code;

    private String label;

    private String description;

    private PropertyTypeDataType dataType;

    private Vocabulary vocabulary;

    public PropertyType()
    {
        this.code = UUID.randomUUID().toString();
        this.label = "label of " + this.code;
        this.description = "description";
        this.dataType = PropertyTypeDataType.BOOLEAN;
        this.vocabulary = null;
    }

    public PropertyType(Vocabulary vocabulary)
    {
        this();
        this.dataType = PropertyTypeDataType.CONTROLLED_VOCABULARY;
        this.vocabulary = vocabulary;
    }

    public String getCode()
    {
        return code;
    }

    public PropertyType setCode(String code)
    {
        this.code = code;
        return this;
    }

    public String getLabel()
    {
        return label;
    }

    public PropertyType setLabel(String label)
    {
        this.label = label;
        return this;
    }

    public String getDescription()
    {
        return description;
    }

    public PropertyType setDescription(String description)
    {
        this.description = description;
        return this;
    }

    public PropertyTypeDataType getDataType()
    {
        return dataType;
    }

    public PropertyType setDataType(PropertyTypeDataType dataType)
    {
        this.dataType = dataType;
        return this;
    }

    public Vocabulary getVocabulary()
    {
        return vocabulary;
    }

    public PropertyType setVocabulary(Vocabulary vocabulary)
    {
        this.vocabulary = vocabulary;
        return this;
    }

    @Override
    public boolean isRepresentedBy(Map<String, String> row)
    {
        return this.code.equalsIgnoreCase(row.get("Code"));
    }

    @Override
    public String toString()
    {
        return "PropertyType " + this.code;
    }

}
